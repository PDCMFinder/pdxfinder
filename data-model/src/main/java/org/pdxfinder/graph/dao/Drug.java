package org.pdxfinder.graph.dao;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
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
     * @param description               A short description of the drug
     * @param synonyms                  Synonyms of the drug
     */

    @Id @GeneratedValue
    private Long id;

    private String name;
    private String target;
    private String manufacturer;

    private String description;
    private String synonyms;


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


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(String synonyms) {
        this.synonyms = synonyms;
    }
}
