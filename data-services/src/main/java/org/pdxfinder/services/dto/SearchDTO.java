package org.pdxfinder.services.dto;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

import java.util.List;

/**
 * Represents an object with search results
 */
public class SearchDTO {

    private String searchTerm;

    public SearchDTO(String searchTerm) {
        this.searchTerm = searchTerm;
    }
}
