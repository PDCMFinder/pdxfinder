package org.pdxfinder.graph.dao;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import org.neo4j.ogm.annotation.NodeEntity;

/*
 * Created by csaba on 09/03/2018.
 */
@NodeEntity
public class DataProjection {

    @Id @GeneratedValue
    private Long id;

    private String label;

    private String value;


    public DataProjection() {
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
