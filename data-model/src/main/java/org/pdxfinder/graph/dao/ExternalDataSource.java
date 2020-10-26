package org.pdxfinder.graph.dao;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import java.util.Date;
import java.util.List;

/**
 * ExternalDataSource represents an external data provider
 */
@NodeEntity
public class ExternalDataSource {

    @Id @GeneratedValue
    private Long id;

    private String name;
    private String abbreviation;
    private String description;
    private String contact;
    private Date dateLastUpdated;

    private String url;
    private String type;
    private List<String> serviceProvided;

    public ExternalDataSource() {
        // Empty constructor required as of Neo4j API 2.0.5
    }

    public ExternalDataSource(String name, String abbreviation, String description,String contact, Date dateLastUpdated) {
        this.name = name;
        this.abbreviation = abbreviation;
        this.description = description;
        this.dateLastUpdated = dateLastUpdated;
        this.contact = contact;
    }

    public ExternalDataSource(String name, String abbreviation, String description, String contact, Date dateLastUpdated, String url) {
        this.name = name;
        this.abbreviation = abbreviation;
        this.description = description;
        this.contact = contact;
        this.dateLastUpdated = dateLastUpdated;
        this.url = url;
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

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public Date getDateLastUpdated() {
        return dateLastUpdated;
    }

    public void setDateLastUpdated(Date dateLastUpdated) {
        this.dateLastUpdated = dateLastUpdated;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getServiceProvided() {
        return serviceProvided;
    }

    public void setServiceProvided(List<String> serviceProvided) {
        this.serviceProvided = serviceProvided;
    }
}
