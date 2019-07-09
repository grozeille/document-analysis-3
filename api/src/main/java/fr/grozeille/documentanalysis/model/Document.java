package fr.grozeille.documentanalysis.model;

import lombok.Data;

@Data
public class Document {
    private String id;

    private String name;

    private String url;

    private String urlTxt;

    private String body;
}
