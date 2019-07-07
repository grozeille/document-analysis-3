package fr.grozeille.documentanalysis.rest;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.common.base.Strings;
import fr.grozeille.documentanalysis.model.Document;
import fr.grozeille.documentanalysis.model.SearchResult;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrOperations;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    @Autowired
    private SolrOperations solrOperations;

    @Autowired
    private DB db;

    private ConcurrentMap<String, String> translationCache;

    private Translate translate;

    @PostConstruct
    public void init() {
        translate = TranslateOptions.getDefaultInstance().getService();

        translationCache = db
                .hashMap("translationCache", Serializer.STRING, Serializer.STRING)
                .createOrOpen();
    }

    @ApiOperation(
            value = "Search",
            notes = "Search for documents")
    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.GET,
            value = "/")
    public ResponseEntity<SearchResult> search(@RequestParam  String query, @RequestParam(defaultValue = "0") Integer page) throws IOException, SolrServerException {

        final int pageSize = 20;

        String queryTranslated = translationCache.get(query);
        if(Strings.isNullOrEmpty(queryTranslated)) {
            // translate query EN to SV
            Translation translation =
                    translate.translate(
                            query,
                            Translate.TranslateOption.sourceLanguage("en"),
                            Translate.TranslateOption.targetLanguage("sv"),
                            // Use "base" for standard edition, "nmt" for the premium model.
                            Translate.TranslateOption.model("base"),
                            Translate.TranslateOption.format("text"));

            queryTranslated = translation.getTranslatedText();
            translationCache.putIfAbsent(query, queryTranslated);
            db.commit();
            log.info("Google Translation of "+query+": "+queryTranslated);
        }
        else {
            log.info("Translation from cache of "+query+": "+queryTranslated);
        }

        SolrQuery solrQuery = new SolrQuery("body_txt:("+query+" OR "+queryTranslated+")");
        solrQuery.addHighlightField("body_txt");
        solrQuery.setHighlight(true);
        solrQuery.setStart(page * pageSize);
        solrQuery.setRows(pageSize);

        QueryResponse response = solrOperations.getSolrClient().query("mycore", solrQuery);

        Map<String, Document> resultMap = new HashMap<>();
        for(SolrDocument d : response.getResults()) {
            Document document = new Document();
            document.setId(d.get("id").toString());

            document.setName(d.get("name_s").toString());
            document.setUrl(d.get("id").toString());

            resultMap.put(d.get("id").toString(), document);
        }

        for(Map.Entry<String, Map<String, List<String>>> h : response.getHighlighting().entrySet()) {
            Document document = resultMap.get(h.getKey());
            List<String> highlights = h.getValue().get("body_txt");
            if(highlights != null && !highlights.isEmpty()) {
                document.setBody(highlights.get(0));
            }
        }

        SearchResult searchResult = new SearchResult();
        searchResult.setNumFound(response.getResults().getNumFound());
        searchResult.setDocuments(resultMap.values().toArray(new Document[0]));

        return ResponseEntity.status(200).body(searchResult);
    }

    @GetMapping(name="/{id}", produces = "application/json")
    public @ResponseBody String getDocument(@PathVariable int id) {
        return null;
    }
}
