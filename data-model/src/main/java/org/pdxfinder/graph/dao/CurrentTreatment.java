package org.pdxfinder.graph.dao;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import org.neo4j.ogm.annotation.NodeEntity;

/*
 * Created by csaba on 24/07/2018.
 */
@NodeEntity
public class CurrentTreatment {

    @Id @GeneratedValue
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
