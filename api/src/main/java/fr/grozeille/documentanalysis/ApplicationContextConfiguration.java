package fr.grozeille.documentanalysis;

import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.solr.core.SolrOperations;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;
import org.springframework.data.solr.server.support.EmbeddedSolrServerFactory;
import org.springframework.data.solr.server.support.HttpSolrClientFactory;

import java.io.File;

@Configuration
@EnableSolrRepositories
@Slf4j
public class ApplicationContextConfiguration {

    @Autowired
    private ApplicationConfiguration configuration;

    @Bean
    public SolrClient solrClient() {
        HttpSolrClient httpSolrClient = new HttpSolrClient.Builder()
                .withBaseSolrUrl(configuration.getSolrUrl())
                .build();

        return  httpSolrClient;
    }

    @Bean(destroyMethod = "close")
    public DB translationDB() {
        DB db = null;
        try {
            db = DBMaker
                    .fileDB(configuration.getTranslationCacheFilePath())
                    .fileMmapEnable()
                    .make();
            db.commit();
            return db;
        }catch(org.mapdb.DBException dbe) {
            log.error("Unable to create cache db: "+dbe.getMessage(), dbe);
            File file = new File(configuration.getTranslationCacheFilePath());
            if(file.exists()){
                file.delete();
            }
            db = DBMaker
                    .fileDB(configuration.getTranslationCacheFilePath())
                    .fileMmapEnable()
                    .make();
            db.commit();
            return db;
        }
    }

    @Bean
    public SolrOperations solrTemplate() {
        return new SolrTemplate(solrClient());
    }
}
