package fr.grozeille.documentanalysis.service;

import com.google.common.base.Strings;
import fr.grozeille.documentanalysis.model.ExtractDocument;
import fr.grozeille.documentanalysis.model.ParsedDocument;
import fr.grozeille.documentanalysis.model.RawDocument;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.ooxml.POIXMLProperties;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.tika.Tika;
import org.apache.tika.langdetect.OptimaizeLangDetector;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.language.detect.LanguageResult;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.ExpandedTitleContentHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrOperations;
import org.springframework.stereotype.Service;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


import javax.xml.transform.OutputKeys;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class DocumentIndexer {

    private static final String[] blacklist = new String[]{
            ".DS_Store", "Thumbs.db"
    };

    private static final String[] extensionWhitelist = new String[]{
            "doc", "docx", "xls", "xlsx", "ppt", "pptx", "pdf"
    };

    private static final int batchSize = 100;

    @Autowired
    private SolrOperations solrOperations;

    private List<SolrInputDocument> solrBatch = new ArrayList<>();

    public void indexPath(String inputPath, String index) throws IOException, SolrServerException {
        List<String> pathList = new ArrayList<>();
        pathList.add(new File(inputPath).toString());
        pathList.addAll(scanSubfolders(inputPath));
        log.info("Folders to analyse:");
        pathList.forEach(log::info);

        // clear the index
        solrOperations.getSolrClient().deleteByQuery(index, "*:*");

        // list all files to write in avro
        for (String path : pathList) {

            File pathFile = new File(path);
            File[] files = pathFile.listFiles();
            if(files == null){
                continue;
            }

            for (File child : files) {
                if (child.isFile()) {

                    if(isBlacklisted(child)){
                        continue;
                    }

                    String extension = FilenameUtils.getExtension(child.getAbsolutePath());

                    if ("zip".equalsIgnoreCase(extension)) {

                        try (InputStream stream = new FileInputStream(child)) {
                            ZipArchiveInputStream zis = new ZipArchiveInputStream(stream);

                            scanZipArchive(index, inputPath, child.getAbsolutePath(), zis);
                        }
                    } else {
                        if(isWhitelisted(child.getName())) {
                            RawDocument rawDocument = toDocument(inputPath, child);
                            parseAndIndex(index, rawDocument);
                        }
                    }
                }
            }
        }

        if(solrBatch.size() > 0) {
            solrOperations.getSolrClient().add(index, solrBatch);
            solrOperations.getSolrClient().commit(index);
        }
    }

    private void parseAndIndex(String index, RawDocument rawDocument) throws IOException {

        LanguageDetector detector = new OptimaizeLangDetector().loadModels();

        ParsedDocument outputDocument = new ParsedDocument();
        String path = rawDocument.getPath();

        outputDocument.setPath(path);
        File inputDocumentFile = new File(path);
        outputDocument.setName(inputDocumentFile.getName());
        outputDocument.setExtension(FilenameUtils.getExtension(inputDocumentFile.getName()));

        // parse the body
        ByteBuffer byteBody = (ByteBuffer) rawDocument.getBody();

        String md5 = DigestUtils.md5Hex(byteBody.array()).toUpperCase();
        outputDocument.setMd5(md5);

        ExtractDocument extractDocument = parseBody(path, byteBody.array());
        String body = extractDocument.getHtmlBody();
        outputDocument.setBody(body);
        outputDocument.setLang("");

        // detect the lang
        LanguageResult result = detector.detect(body);
        if(result.isReasonablyCertain()) {
            outputDocument.setLang(result.getLanguage());
        }
        else {
            log.warn("Unable to detect language " + path);
        }

        try {
            indexToSolr(index, outputDocument);
        }
        catch(Exception ex) {
            log.info(outputDocument.getName());
            log.error(ex.getMessage(), ex);
        }
    }

    private void indexToSolr(String index, ParsedDocument inputDocument) throws IOException, SolrServerException {
        String id = DigestUtils.sha256Hex(inputDocument.getPath());

        SolrInputDocument solrDocument = new SolrInputDocument();
        String path = inputDocument.getPath();
        String md5 = inputDocument.getMd5();
        String name = inputDocument.getName();
        String extension = inputDocument.getExtension();
        String lang = inputDocument.getLang();
        String body = inputDocument.getBody();

        Document doc = Jsoup.parse(body, "UTF-8");
        //for(Element divPage : doc.select("meta")) {
        // TODO String html = divPage.html();
        //}

        StringBuilder bodyTextBuilder = new StringBuilder();
        for(Element divPage : doc.select("div[class='page']")) {
            for(Element p : divPage.select("p")) {
                bodyTextBuilder.append(p.text()).append("\n\n");
            }
            bodyTextBuilder.append("\n\n");
        }
        String bodyText = bodyTextBuilder.toString();

        solrDocument.addField("id", id);
        solrDocument.addField("path_descendent_path", path);
        solrDocument.addField("path_txt", path);
        solrDocument.addField("name_s", name);
        solrDocument.addField("name_txt", name);
        solrDocument.addField("extension_s", extension);
        solrDocument.addField("md5_s", md5);
        solrDocument.addField("body_txt", bodyText);
        solrDocument.addField("lang_s", lang);
        if(!Strings.isNullOrEmpty(lang)) {
            solrDocument.addField("body_txt_"+lang, bodyText);
        }
        solrBatch.add(solrDocument);

        if(solrBatch.size() >= batchSize) {
            solrOperations.getSolrClient().add(index, solrBatch);
            solrOperations.getSolrClient().commit(index);
            solrBatch = new ArrayList<>();
        }
    }

    private void scanZipArchive(String index, String rootPath, String parentPath, ZipArchiveInputStream zis) throws IOException {

        log.debug("Scan zip file: "+parentPath);
        try {
            ArchiveEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {

                    String entryPath = parentPath + "/" + entry.getName();

                    if ("zip".equalsIgnoreCase(FilenameUtils.getExtension(entry.getName()))) {
                        ZipArchiveInputStream subZis = new ZipArchiveInputStream(zis);
                        scanZipArchive(index, rootPath, entryPath, subZis);
                    } else {
                        if(isWhitelisted(entry.getName())) {
                            RawDocument rawDocument = toDocument(rootPath, entryPath, zis);

                            parseAndIndex(index, rawDocument);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Unable to read zip file " + parentPath, ex);
        }
    }


    private static RawDocument toDocument(String rootPath, String path, InputStream stream) throws IOException {
        RawDocument doc = new RawDocument();
        Path relativePath = Paths.get(rootPath).relativize(Paths.get(path));
        doc.setPath(relativePath.toString());
        doc.setLang("");
        org.apache.commons.io.output.ByteArrayOutputStream ous = new ByteArrayOutputStream();
        IOUtils.copy(stream, ous);
        doc.setBody(ByteBuffer.wrap(ous.toByteArray()));

        return doc;
    }

    private static RawDocument toDocument(String rootPath, File file) throws IOException {
        try(InputStream stream = new FileInputStream(file)) {
            return toDocument(rootPath, file.getAbsolutePath(), stream);
        }
    }

    private static boolean isBlacklisted(File file){
        for(String b : blacklist){
            if(b.equalsIgnoreCase(file.getName())){
                return true;
            }
        }

        return false;
    }

    private static boolean isWhitelisted(String fileName){
        for(String b : extensionWhitelist){
            if(b.equalsIgnoreCase(FilenameUtils.getExtension(fileName))){
                return true;
            }
        }

        return false;
    }

    private static Collection<? extends String> scanSubfolders(String inputPath) {
        File parent = new File(inputPath);
        List<String> folders = new ArrayList<>();
        String[] directories = parent.list((current, name) -> new File(current, name).isDirectory());
        if(directories != null) {
            for (String d : directories) {
                folders.add(new File(parent, d).toString());
                folders.addAll(scanSubfolders(new File(parent, d).getAbsolutePath()));
            }
        }
        return folders;
    }

    private static Tika tika;
    private static Metadata metadata;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private static ExtractDocument parseBody(String path, byte[] byteBody) throws IOException {
        ExtractDocument result = new ExtractDocument();

        if(tika == null) {
            tika = new Tika();
            metadata = new Metadata();
            metadata.add(Metadata.CONTENT_ENCODING, "UTF-8");
        }

        ByteArrayInputStream inputStream = new ByteArrayInputStream(byteBody);

        log.info("Parsing file: "+path);

        String extension = FilenameUtils.getExtension(path);

        // not very good... but need to read it multiple times and tika is closing the stream at the end of the parsing...
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        IOUtils.copy(inputStream, out);
        IOUtils.closeQuietly(out);
        byte[] bytes = out.toByteArray();


        AutoDetectParser parser = new AutoDetectParser();
        Metadata metadata = new Metadata();

        // parse document
        try(ByteArrayInputStream stream = new ByteArrayInputStream(bytes)) {
            try {

                java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
                SAXTransformerFactory factory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
                TransformerHandler handler = factory.newTransformerHandler();
                handler.getTransformer().setOutputProperty(OutputKeys.METHOD, "html");
                handler.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
                handler.getTransformer().setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                handler.setResult(new StreamResult(bos));
                ExpandedTitleContentHandler handler1 = new ExpandedTitleContentHandler(handler);

                parser.parse(stream, handler1, new Metadata());
                String htmlText = new String(bos.toByteArray(), "UTF-8");

                result.setHtmlBody(htmlText);

            }catch (Exception ex){
                log.error("Unable to parse file: "+path, ex);
                result.setHtmlBody("");
            }
        }

        if("pdf".equalsIgnoreCase(extension)){

            try {

                try(ByteArrayInputStream stream = new ByteArrayInputStream(bytes)) {

                    // load all pages of the PDF and search for images
                    try (PDDocument document = PDDocument.load(stream)) {

                        PDDocumentInformation info = document.getDocumentInformation();


                        result.getMetadata().put("title", info.getTitle());
                        result.getMetadata().put("author", info.getAuthor());
                        result.getMetadata().put("subject", info.getSubject());
                        result.getMetadata().put("keywords", info.getKeywords());
                        result.getMetadata().put("creator", info.getCreator());
                        result.getMetadata().put("producer", info.getProducer());
                        result.getMetadata().put("creationDate", info.getCreationDate() != null ? dateFormat.format(info.getCreationDate().getTime()) : "");
                        result.getMetadata().put("modificationDate", info.getModificationDate() != null ? dateFormat.format(info.getModificationDate().getTime()) : "");

                        if(document.isEncrypted()) {
                            log.warn("Unable to decrypt PDF: " + path);
                        }
                    }
                }
            }
            catch (Exception ex){
                log.error("Unable to parse PDF document", ex);
            }
        }
        else if("docx".equalsIgnoreCase(extension)){
            try {
                try(ByteArrayInputStream stream = new ByteArrayInputStream(bytes)) {

                    try (XWPFDocument document = new XWPFDocument(stream)) {
                        POIXMLProperties.CoreProperties props = document.getProperties().getCoreProperties();
                        result.getMetadata().put("title", props.getTitle());
                        result.getMetadata().put("description", props.getDescription());
                        result.getMetadata().put("creator", props.getCreator());
                        result.getMetadata().put("keywords", props.getKeywords());
                        result.getMetadata().put("subject", props.getSubject());
                    }
                }
            }
            catch (Exception ex){
                log.error("Unable to parse DOCX document", ex);
            }
        }
        else if("doc".equalsIgnoreCase(extension)){
            try {
                try(ByteArrayInputStream stream = new ByteArrayInputStream(bytes)) {
                    SummaryInformation si = (SummaryInformation) PropertySetFactory.create(stream);

                    result.getMetadata().put("title", si.getTitle());
                    result.getMetadata().put("lastAuthor", si.getLastAuthor());
                    result.getMetadata().put("author", si.getAuthor());
                    result.getMetadata().put("keywords", si.getKeywords());
                    result.getMetadata().put("comments", si.getComments());
                    result.getMetadata().put("subject", si.getSubject());
                }
            }
            catch (Exception ex){
                log.error("Unable to parse DOC document", ex);
            }
        }
        else if("pptx".equalsIgnoreCase(extension)){
            try {
                try(ByteArrayInputStream stream = new ByteArrayInputStream(bytes)) {

                    try (XMLSlideShow document = new XMLSlideShow(stream)) {
                        POIXMLProperties.CoreProperties props = document.getProperties().getCoreProperties();
                        result.getMetadata().put("title", props.getTitle());
                        result.getMetadata().put("description", props.getDescription());
                        result.getMetadata().put("creator", props.getCreator());
                        result.getMetadata().put("keywords", props.getKeywords());
                        result.getMetadata().put("subject", props.getSubject());
                    }
                }
            }
            catch (Exception ex){
                log.error("Unable to parse DOCX document", ex);
            }
        }

        return result;
    }
}
