package fr.grozeille.documentanalysis.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ExtractDocument {
    private String htmlBody;

    private Map<String, String> metadata = new HashMap<>();
}
