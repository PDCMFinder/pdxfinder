package org.pdxfinder.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

/*
 * Created by csaba on 24/07/2018.
 */
@NodeEntity
public class CurrentTreatment {

    @GraphId
    private Long id;

    private String name;


    public CurrentTreatment() {
    }


    public CurrentTreatment(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
