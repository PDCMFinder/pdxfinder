package org.pdxfinder.graph.dao;

import org.neo4j.ogm.annotation.NodeEntity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Sample type represents the various types of tumors
 */
@NodeEntity
public class TumorType {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private TumorType() {
        // Empty constructor required as of Neo4j API 2.0.5
    }

    public TumorType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
