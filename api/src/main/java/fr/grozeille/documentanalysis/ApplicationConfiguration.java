package fr.grozeille.documentanalysis;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "document-analysis")
@Data
public class ApplicationConfiguration {
    private String solrUrl;
}
