package fr.grozeille.documentanalysis.rest;

import fr.grozeille.documentanalysis.ApplicationConfiguration;
import fr.grozeille.documentanalysis.model.Document;
import fr.grozeille.documentanalysis.model.SearchResult;
import fr.grozeille.documentanalysis.service.DocumentIndexer;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrOperations;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    @Autowired
    private ApplicationConfiguration configuration;

    @Autowired
    private SolrOperations solrOperations;

    @Autowired
    private DocumentIndexer documentIndexer;

    @PostConstruct
    public void init() {
    }

    @ApiOperation(
            value = "Search",
            notes = "Search for documents")
    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.GET,
            value = "/")
    public ResponseEntity<SearchResult> search(@RequestParam  String query, @RequestParam(defaultValue = "0") Integer page) throws IOException, SolrServerException {

        final int pageSize = 20;

        query = query.toLowerCase();

        String bodyQuery = "body_txt_en:"+query+" OR body_txt_fr:"+query;;
        String nameQuery = "name_txt:"+query;
        String pathQuery = "path_txt:"+query;

        SolrQuery solrQuery = new SolrQuery(String.join(" OR ", Arrays.asList(bodyQuery, nameQuery, pathQuery)));
        solrQuery.addHighlightField("body_txt_en");
        //solrQuery.addHighlightField("body_txt_sv");
        solrQuery.addHighlightField("name_txt");
        solrQuery.addHighlightField("path_txt");
        solrQuery.setHighlight(true);
        solrQuery.setHighlightFragsize(512);
        // priority to name and path
        solrQuery.set("defType","edismax");
        solrQuery.set("bq",String.join(" OR ", Arrays.asList(nameQuery, pathQuery)));
        solrQuery.set("qf","body_txt_en^1.0 body_txt_fr^1.0 name_txt^4.0 path_txt^3.0");
        solrQuery.setStart(page * pageSize);
        solrQuery.setRows(pageSize);

        QueryResponse response = solrOperations.getSolrClient().query(this.configuration.getSolrIndex(), solrQuery);

        Map<String, Document> resultMap = new LinkedHashMap<>();
        for(SolrDocument d : response.getResults()) {
            Document document = new Document();
            document.setId(d.get("id").toString());

            document.setName(d.get("name_s").toString());
            document.setUrl(d.get("path_descendent_path").toString());
            document.setUrlTxt(document.getUrl());

            Collection<Object> tags = d.getFieldValues("tags_ss");
            List<String> documentTags = new ArrayList<>();
            if(tags != null) {
                for(Object t : tags) {
                    documentTags.add(t.toString());
                }
            }
            document.setTags(documentTags.toArray(new String[0]));

            resultMap.put(d.get("id").toString(), document);
        }

        for(Map.Entry<String, Map<String, List<String>>> h : response.getHighlighting().entrySet()) {
            Document document = resultMap.get(h.getKey());
            List<String> highlights = h.getValue().get("body_txt_en");
            if(highlights != null && !highlights.isEmpty()) {
                document.setBody(highlights.get(0));
            }
            else {
                highlights = h.getValue().get("body_txt_fr");
                if(highlights != null && !highlights.isEmpty()) {
                    document.setBody(highlights.get(0));
                }
            }

            highlights = h.getValue().get("name_txt");
            if(highlights != null && !highlights.isEmpty()) {
                document.setName(highlights.get(0));
            }
            highlights = h.getValue().get("path_txt");
            if(highlights != null && !highlights.isEmpty()) {
                document.setUrlTxt(highlights.get(0));
            }
        }

        SearchResult searchResult = new SearchResult();
        searchResult.setNumFound(response.getResults().getNumFound());
        searchResult.setDocuments(resultMap.values().toArray(new Document[0]));

        CacheControl cacheControl = CacheControl.maxAge(30, TimeUnit.SECONDS).cachePrivate();

        return ResponseEntity
                .status(200)
                .lastModified(System.currentTimeMillis())
                .cacheControl(cacheControl)
                .body(searchResult);
    }

    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.GET,
            value = "/{id}")
    public @ResponseBody ResponseEntity<Document> getDocument(@PathVariable String id) throws IOException, SolrServerException {

        try {
            SolrDocument d = solrOperations.getSolrClient().getById(this.configuration.getSolrIndex(), id);

            Document document = new Document();
            document.setId(d.get("id").toString());

            document.setName(d.get("name_s").toString());
            document.setUrl(d.get("path_descendent_path").toString());
            document.setUrlTxt(document.getUrl());
            document.setBody("");

            Collection<Object> tags = d.getFieldValues("tags_ss");
            List<String> documentTags = new ArrayList<>();
            if(tags != null) {
                for(Object t : tags) {
                    documentTags.add(t.toString());
                }
            }
            document.setTags(documentTags.toArray(new String[0]));


            return ResponseEntity.ok(document);
        } catch(SolrServerException e) {
            return ResponseEntity.notFound().build();
        }

    }

    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.POST,
            value = "/{id}/tags")
    public @ResponseBody ResponseEntity<?> updateTags(@PathVariable String id, @RequestBody String[] tags) throws IOException, SolrServerException {
        try {
            SolrDocument d = solrOperations.getSolrClient().getById(this.configuration.getSolrIndex(), id);

            SolrInputDocument inputDocument = new SolrInputDocument();
            for (String fieldName : d.getFieldNames()) {
                inputDocument.addField(fieldName, d.getFieldValue(fieldName));
            }

            inputDocument.removeField("tags_ss");
            for(String t : tags) {
                inputDocument.addField("tags_ss", t);
            }

            solrOperations.getSolrClient().add(this.configuration.getSolrIndex(), inputDocument);
            solrOperations.commit(this.configuration.getSolrIndex());

            return ResponseEntity.ok().build();
        } catch(SolrServerException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET,
        value = "/{id}/same")
    public @ResponseBody ResponseEntity<SearchResult> getSameDocument(@PathVariable String id) throws IOException, SolrServerException {

        try {
            SolrDocument foundDocument = solrOperations.getSolrClient().getById(this.configuration.getSolrIndex(), id);

            SolrQuery solrQuery = new SolrQuery("name_s:\""+foundDocument.getFieldValue("name_s").toString()+"\"");
            QueryResponse response = solrOperations.getSolrClient().query(this.configuration.getSolrIndex(), solrQuery);

            List<Document> documents = new ArrayList<>();
            for(SolrDocument d : response.getResults()) {
                Document document = new Document();
                document.setId(d.get("id").toString());

                document.setName(d.get("name_s").toString());
                document.setUrl(d.get("path_descendent_path").toString());
                document.setUrlTxt(document.getUrl());
                document.setBody("");

                Collection<Object> tags = d.getFieldValues("tags_ss");
                List<String> documentTags = new ArrayList<>();
                if(tags != null) {
                    for(Object t : tags) {
                        documentTags.add(t.toString());
                    }
                }
                document.setTags(documentTags.toArray(new String[0]));

                if(!document.getId().equalsIgnoreCase(id)) {
                    documents.add(document);
                }
            }

            SearchResult result = new SearchResult();
            result.setNumFound(response.getResults().getNumFound());
            result.setDocuments(documents.toArray(new Document[0]));

            return ResponseEntity.ok(result);
        } catch(SolrServerException e) {
            return ResponseEntity.notFound().build();
        }

    }

    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.POST,
            value = "/index")
    public void index(@RequestParam  String path) throws IOException, InterruptedException, SolrServerException {
        long startDate = System.currentTimeMillis();

        documentIndexer.indexPath(path, this.configuration.getSolrIndex());

        long executionTime = System.currentTimeMillis()-startDate;

        log.info("Index finished in " + (executionTime / 1000.0) + " sec");
    }

}
