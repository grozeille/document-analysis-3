package fr.grozeille.documentanalysis.model;

import lombok.Data;

@Data
public class ParsedDocument {
    private String path;
    private String md5;
    private String name;
    private String extension;
    private String lang;
    private String body;
}
