package fr.grozeille.documentanalysis.model;

import lombok.Data;

import java.nio.ByteBuffer;

@Data
public class RawDocument {
    private String path;
    private ByteBuffer body;
    private String lang;
}
