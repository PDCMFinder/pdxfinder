package org.pdxi.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * Tissue represents a human tissue associated with a tumor
 */
@NodeEntity
public class Tissue {

    @GraphId
    private Long id;

    private String providedName;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
