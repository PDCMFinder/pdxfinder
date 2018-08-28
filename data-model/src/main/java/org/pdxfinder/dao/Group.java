package org.pdxfinder.dao;

/*
 * Created by csaba on 26/06/2018.
 */

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;


@NodeEntity
public class Group {

    @GraphId
    private Long id;

    private String name; //The name of the group, ie: The Jackson Laboratory
    private String type; // Group type: Provider, Project, Publication
    private String providerType; // Academia, Industry
    private String accessibility; //for provider group only: Academia only, Academia and Industry
    private String accessModalities; //transnational access, collaboration only (this is specific to EuroPDX)

    private String abbreviation; // used for Provider type only
    private String description; // The description of the group

    private String contact;

    private String pubMedId;
    private String publicationTitle;
    private String authors;

    private String url; // a url to their website

    public Group() {
    }

    public Group(String name, String abbrev, String type) {
        this.name = name;
        this.type = type;
        this.abbreviation = abbrev;
    }


    //Special constructor for Provider Groups
    public Group(String name, String abbrev, String description, String providerType, String accessibility,
                 String accessModalities, String contact, String url){

        this.name = name;
        this.abbreviation = abbrev;
        this.description = description;
        this.providerType = providerType;
        this.accessibility = accessibility;
        this.accessModalities = accessModalities;
        this.contact = contact;
        this.url = url;
        this.type = "Provider";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getProviderType() {
        return providerType;
    }

    public void setProviderType(String providerType) {
        this.providerType = providerType;
    }

    public String getAccessibility() {
        return accessibility;
    }

    public void setAccessibility(String accessibility) {
        this.accessibility = accessibility;
    }

    public String getAccessModalities() {
        return accessModalities;
    }

    public void setAccessModalities(String accessModalities) {
        this.accessModalities = accessModalities;
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

    public String getPubMedId() {
        return pubMedId;
    }

    public void setPubMedId(String pubMedId) {
        this.pubMedId = pubMedId;
    }

    public String getPublicationTitle() {
        return publicationTitle;
    }

    public void setPublicationTitle(String publicationTitle) {
        this.publicationTitle = publicationTitle;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
