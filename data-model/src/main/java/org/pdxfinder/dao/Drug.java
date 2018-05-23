package org.pdxfinder.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

/*
 * Created by csaba on 22/05/2018.
 */
@NodeEntity
public class Drug {


    /**
     * @param name                      The name of the drug
     * @param target                    Drug target, ie DNA, EGFR (first target corresponds to the first drug, etc)
     * @param manufacturer              What company manufactured the drug
     */

    @GraphId
    private Long id;

    private String name;
    private String target;
    private String manufacturer;

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

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }
}
