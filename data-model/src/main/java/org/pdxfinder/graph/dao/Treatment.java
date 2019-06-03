package org.pdxfinder.graph.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

/*
 * Created by csaba on 08/05/2019.
 */
@NodeEntity
public class Treatment {


    @GraphId
    private Long id;

    private String name;



    public Treatment(String name) {
        this.name = name;
    }

    public Treatment() {
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
