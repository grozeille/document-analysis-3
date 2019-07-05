package fr.grozeille;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.file.CodecFactory;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.reflect.ReflectDatumWriter;
import org.apache.commons.cli.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.ooxml.POIXMLProperties;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.tika.Tika;
import org.apache.tika.langdetect.OptimaizeLangDetector;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.language.detect.LanguageResult;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.*;
import fr.grozeille.avro.ParsedDocument;
import fr.grozeille.avro.RawDocument;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class DocumentExtractor {

    public static void main(String[] args) throws Exception {

        Option inputOption  = OptionBuilder.withArgName( "input" )
                .isRequired()
                .hasArgs()
                .withDescription( "Input path to analyse." )
                .create( "i" );
        Option outputOption  = OptionBuilder.withArgName( "output" )
                .isRequired()
                .hasArgs()
                .withDescription( "Output path for avro files." )
                .create( "o" );

        Options options = new Options();
        options.addOption(inputOption);
        options.addOption(outputOption);

        // create the parser
        CommandLineParser parser = new BasicParser();
        CommandLine line = null;
        try {
            // parse the command line arguments
            line = parser.parse( options, args );
        }
        catch( ParseException exp ) {
            // oops, something went wrong
            System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "DetectLang", options );

            System.exit(-1);
        }

        //Schema.Parser parser = new Schema.Parser();
        //Schema avroSchema = parser.parse("");
        Schema rawDocumentAvroSchema = RawDocument.SCHEMA$;


        String inputPath = line.getOptionValue("i");
        String outputPath = line.getOptionValue("o");

        File parentOutputFolder = new File(outputPath);
        if(parentOutputFolder.exists()){
            System.err.println("Folder "+outputPath+" already exist");
            log.error("Folder "+outputPath+" already exist");
            System.exit(-1);
        }

        parentOutputFolder.mkdirs();


        File pathFile = new File(inputPath);
        File[] files = pathFile.listFiles();
        if(files == null){
            log.warn("No files in " + inputPath);
            return;
        }

        //final DatumReader<Document> reader = new ReflectDatumReader<>(Document.class);
        final GenericDatumReader<GenericRecord> reader = new GenericDatumReader<>();


        List<String> supportedLanguages = Arrays.asList(
                "ar",
                "bg",
                "ca",
                "cjk",
                "cz",
                "da",
                "de",
                "el",
                "en",
                "es",
                "eu",
                "fa",
                "fi",
                "fr",
                "ga",
                "gl",
                "hi",
                "hu",
                "hy",
                "id",
                "it",
                "ja",
                "ko",
                "lv",
                "nl",
                "no",
                "pt",
                "rev",
                "ro",
                "ru",
                "sv",
                "th",
                "tr"
        );
        LanguageDetector detector = new OptimaizeLangDetector().loadModels();
        //Translate translate = TranslateOptions.getDefaultInstance().getService();

        for(File inputFile : files) {
            if(inputFile.isDirectory()) {
                continue;
            }

            File outputFile = new File(parentOutputFolder, inputFile.getName());

            try(DataFileReader<GenericRecord> dataFileReader = new DataFileReader<>(inputFile, reader)) {
                try(DataFileWriter<ParsedDocument> dataFileWriter = createAvroDocumentFile(outputFile)) {

                    for(GenericRecord inputDocument : dataFileReader) {

                        ParsedDocument outputDocument = new ParsedDocument();
                        String path = inputDocument.get("path").toString();

                        outputDocument.setPath(path);
                        File inputDocumentFile = new File(path);
                        outputDocument.setName(inputDocumentFile.getName());
                        outputDocument.setExtension(FilenameUtils.getExtension(inputDocumentFile.getName()));

                        // parse the body
                        ByteBuffer byteBody = (ByteBuffer) inputDocument.get("body");

                        String md5 = DigestUtils.md5Hex(byteBody.array()).toUpperCase();
                        outputDocument.setMd5(md5);

                        ExtractDocument extractDocument = parseBody(path, byteBody.array());
                        String body = extractDocument.getHtmlBody();
                        outputDocument.setBody(body);
                        //outputDocument.setBodyTranslated("");
                        outputDocument.setLang("");

                        // detect the lang
                        // translate.detect(body).
                        LanguageResult result = detector.detect(body);
                        if(result.isReasonablyCertain()) {
                            outputDocument.setLang(result.getLanguage());

                            if(supportedLanguages.contains(result.getLanguage())) {

                                /*AttributeFactory factory = AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY;

                                StandardTokenizer tokenizer = new StandardTokenizer(factory);
                                tokenizer.setReader(new StringReader(body));
                                tokenizer.reset();

                                List<String> tokenList = new ArrayList<>();
                                CharTermAttribute attr = tokenizer.addAttribute(CharTermAttribute.class);
                                while (tokenizer.incrementToken()) {
                                    String term = attr.toString();

                                    tokenList.add(term);
                                }
                                log.info("Words: "+tokenList.size());*/

                                /*
                                Document doc = Jsoup.parse(body, "UTF-8");
                                for(Element divPage : doc.select("div[class='page']")) {
                                    String html = divPage.html();

                                    try {
                                        Translation translation =
                                                translate.translate(
                                                        html,
                                                        Translate.TranslateOption.sourceLanguage(result.getLanguage()),
                                                        Translate.TranslateOption.targetLanguage("en"),
                                                        // Use "base" for standard edition, "nmt" for the premium model.
                                                        Translate.TranslateOption.model("base"),
                                                        Translate.TranslateOption.format("html"));

                                        String translatedHtml = translation.getTranslatedText();
                                        divPage.replaceWith(new Element(Tag.valueOf("div"), "").addClass("page").html(translatedHtml));
                                    }
                                    catch(TranslateException te) {
                                        if(te.getReason().equals("userRateLimitExceeded")) {
                                            log.warn(te.getMessage() + ", waiting 100s for next quota");
                                            Thread.sleep(100*1000);

                                            Translation translation =
                                                    translate.translate(
                                                            html,
                                                            Translate.TranslateOption.sourceLanguage(result.getLanguage()),
                                                            Translate.TranslateOption.targetLanguage("en"),
                                                            // Use "base" for standard edition, "nmt" for the premium model.
                                                            Translate.TranslateOption.model("base"),
                                                            Translate.TranslateOption.format("html"));

                                            String translatedHtml = translation.getTranslatedText();
                                            divPage.replaceWith(new Element(Tag.valueOf("div"), "").addClass("page").html(translatedHtml));
                                        }
                                        else {
                                            throw te;
                                        }
                                    }
                                }

                                String body_en = doc.toString();
                                outputDocument.setBodyTranslated(body_en);*/
                            }
                        }
                        else {
                            log.warn("Unable to detect language " + path);
                        }

                        try {
                            dataFileWriter.append(outputDocument);
                        }
                        catch(Exception ex) {
                            log.info(outputDocument.getName().toString());
                            log.error(ex.getMessage(), ex);
                        }
                    }

                }
            }
        }
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
        //StringBuilder outputText = new StringBuilder();

        // not very good... but need to read it multiple times and tika is closing the stream at the end of the parsing...
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(inputStream, out);
        IOUtils.closeQuietly(out);
        byte[] bytes = out.toByteArray();

        // add path to text
        //outputText.append(path.replace('.', ' ').replace('/', ' ').replace('\\', ' ').replace('_', ' ')).append("\n");



        AutoDetectParser parser = new AutoDetectParser();
        Metadata metadata = new Metadata();

        // parse document
        try(ByteArrayInputStream stream = new ByteArrayInputStream(bytes)) {
            try {
                /*ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ToHTMLContentHandler toHtmlContentHandler = new ToHTMLContentHandler(bos, "UTF-8");
                WriteOutContentHandler handler = new WriteOutContentHandler(toHtmlContentHandler, (int) 4000000);
                ContentHandler bodyHandler = new BodyContentHandler(handler);

                parser.parse(stream, bodyHandler, metadata);
                String text = bos.toString("UTF-8");*/

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
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

                //stream.reset();
                //String text = tika.parseToString(stream, metadata);
                //String text = tika.parseToString(stream);
                //outputText.append(text).append("\n");
            }catch (Exception ex){
                log.error("Unable to parse file: "+path, ex);
                result.setHtmlBody("");
            }
        }

        // special case for PDF: parse images inside (ocr)
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

    private static DataFileWriter<ParsedDocument> createAvroDocumentFile(File outputFile) throws IOException {
        Schema schema = ReflectData.get().getSchema(ParsedDocument.class);
        DatumWriter<ParsedDocument> writer = new ReflectDatumWriter<>(ParsedDocument.class);
        return new DataFileWriter<>(writer)
                .setCodec(CodecFactory.snappyCodec())
                .create(schema, outputFile);
    }
}
