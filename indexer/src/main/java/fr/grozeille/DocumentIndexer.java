package fr.grozeille;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.cli.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import fr.grozeille.avro.ParsedDocument;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DocumentIndexer {

    public static void main(String[] args) throws Exception {

        // to clean all: curl http://beebox02:8983/solr/mycore/update?commit=true -H "Content-Type: text/xml" --data-binary '<delete><query>*:*</query></delete>'

        Option inputOption  = OptionBuilder.withArgName( "input" )
                .isRequired()
                .hasArgs()
                .withDescription( "Input path to analyse." )
                .create( "i" );

        Options options = new Options();
        options.addOption(inputOption);

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
        Schema parsedDocumentAvroSchema = ParsedDocument.SCHEMA$;

        HttpSolrClient httpSolrClient = new HttpSolrClient.Builder()
                .withBaseSolrUrl("http://beebox02:8983/solr/mycore")
                .build();


        String inputPath = line.getOptionValue("i");


        File pathFile = new File(inputPath);
        File[] files = pathFile.listFiles();
        if(files == null){
            log.warn("No files in " + inputPath);
            return;
        }

        //final DatumReader<Document> reader = new ReflectDatumReader<>(Document.class);
        final GenericDatumReader<GenericRecord> reader = new GenericDatumReader<>();

        int batchSize = 100;
        List<SolrInputDocument> solrBatch = new ArrayList<>();

        for(File inputFile : files) {
            if(inputFile.isDirectory()) {
                continue;
            }

            try(DataFileReader<GenericRecord> dataFileReader = new DataFileReader<>(inputFile, reader)) {
                for(GenericRecord inputDocument : dataFileReader) {

                    String id = DigestUtils.sha256Hex(inputDocument.get("path").toString());

                    SolrInputDocument solrDocument = new SolrInputDocument();
                    String path = inputDocument.get("path").toString();
                    String md5 = inputDocument.get("md5").toString();
                    String name = inputDocument.get("name").toString();
                    String extension = inputDocument.get("extension").toString();
                    String lang = inputDocument.get("lang").toString();
                    String body = inputDocument.get("body").toString();

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
                        httpSolrClient.add(solrBatch);
                        httpSolrClient.commit();
                        solrBatch = new ArrayList<>();
                    }
                }
            }

        }

        if(solrBatch.size() > 0) {
            httpSolrClient.add(solrBatch);
            httpSolrClient.commit();
        }
    }
}
