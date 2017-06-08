package org.pdxfinder.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Set;

/**
 * Created by csaba on 07/06/2017.
 */
@NodeEntity
public class OntologyTerm {

    @GraphId
    private Long id;

    private String url;
    private String label;

    @Relationship(type = "SUBCLASS_OF" ,direction = Relationship.OUTGOING)
    private Set<OntologyTerm> subclassOf;

    @Relationship(type = "MAPPED_TO", direction = Relationship.INCOMING)
    private Set<Sample> mappedTo;


    public OntologyTerm() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Set<OntologyTerm> getSubclassOf() {
        return subclassOf;
    }

    public void setSubclassOf(Set<OntologyTerm> subclassOf) {
        this.subclassOf = subclassOf;
    }

    public Set<Sample> getMappedTo() {
        return mappedTo;
    }

    public void setMappedTo(Set<Sample> mappedTo) {
        this.mappedTo = mappedTo;
    }
}
