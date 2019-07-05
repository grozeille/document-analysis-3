package fr.grozeille.documentanalysis.model;

import lombok.Data;

@Data
public class SearchResult {
    private Document[] documents;

    private long numFound;
}
