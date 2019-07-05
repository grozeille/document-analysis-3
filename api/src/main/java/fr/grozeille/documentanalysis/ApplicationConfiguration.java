package fr.grozeille.documentanalysis;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.solr.core.SolrOperations;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;
import org.springframework.data.solr.server.support.EmbeddedSolrServerFactory;
import org.springframework.data.solr.server.support.HttpSolrClientFactory;

@Configuration
@EnableSolrRepositories
public class ApplicationConfiguration {
    @Bean
    public SolrClient solrClient() {
        HttpSolrClient httpSolrClient = new HttpSolrClient.Builder()
                .withBaseSolrUrl("http://beebox02:8983/solr")
                .build();

        return  httpSolrClient;
    }

    @Bean
    public SolrOperations solrTemplate() {
        return new SolrTemplate(solrClient());
    }
}
