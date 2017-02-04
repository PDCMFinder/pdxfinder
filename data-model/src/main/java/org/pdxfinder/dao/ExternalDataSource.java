package org.pdxfinder.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

import java.util.Date;

/**
 * ExternalDataSource represents an external data provider
 */
@NodeEntity
public class ExternalDataSource {

    @GraphId
    private Long id;

    private String name;
    private String abbreviation;
    private String description;
    private Date dateLastUpdated;

    public ExternalDataSource() {
        // Empty constructor required as of Neo4j API 2.0.5
    }

    public ExternalDataSource(String name, String abbreviation, String description, Date dateLastUpdated) {
        this.name = name;
        this.abbreviation = abbreviation;
        this.description = description;
        this.dateLastUpdated = dateLastUpdated;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDateLastUpdated() {
        return dateLastUpdated;
    }

    public void setDateLastUpdated(Date dateLastUpdated) {
        this.dateLastUpdated = dateLastUpdated;
    }
}
