package org.pdxfinder.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

/*
 * Created by csaba on 22/05/2018.
 */
@NodeEntity
public class Drug {

    @GraphId
    private Long id;

    private String name;
    private String target;

    public Drug() {
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}
