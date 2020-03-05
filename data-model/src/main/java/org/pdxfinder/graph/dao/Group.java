package org.pdxfinder.graph.dao;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.neo4j.ogm.annotation.NodeEntity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@NodeEntity
public class Group {

    @Id
    @GeneratedValue
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

    public Group() { }

    public Group(String name, String abbrev, String type) {
        this.name = name;
        this.type = type;
        this.abbreviation = abbrev;
    }

    public static Group createAccessibilityGroup(
        String accessibility,
        String accessModalities
    ) {
        Group ag = new Group();
        ag.setName("Has access information");
        ag.setType("Accessibility");
        ag.setAccessibility(accessibility);
        ag.setAccessModalities(accessModalities);
        return ag;
    }

    public static Group createProviderGroup(
        String name,
        String abbreviation,
        String description,
        String providerType,
        String contact,
        String url
    ) {
        Group pg = new Group();
        pg.setName(name);
        pg.setType("Provider");
        pg.setProviderType(providerType);
        pg.setAbbreviation(abbreviation);
        pg.setDescription(description);
        pg.setContact(contact);
        pg.setUrl(url);
        return pg;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Group group = (Group) o;

        return new EqualsBuilder()
            .append(getName(), group.getName())
            .append(getType(), group.getType())
            .append(getAccessibility(), group.getAccessibility())
            .append(getAccessModalities(), group.getAccessModalities())
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(getName())
            .append(getType())
            .append(getAccessibility())
            .append(getAccessModalities())
            .toHashCode();
    }

    @Override
    public String toString() {
        return String.format("[%s: %s]", this.type, this.name);
    }

}
