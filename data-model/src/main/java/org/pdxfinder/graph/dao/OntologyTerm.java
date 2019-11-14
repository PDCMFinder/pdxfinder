package org.pdxfinder.graph.dao;

import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by csaba on 07/06/2017.
 */
@NodeEntity
public class OntologyTerm {

    @Id
    @GeneratedValue
    private Long id;

    @Index
    private String url;

    @Index
    private String label;

    private int directMappedSamplesNumber;

    private int indirectMappedSamplesNumber;

    private Set<String> synonyms;

    @Index
    private String type;

    private String description;

    private boolean allowAsSuggestion = true;

    @Relationship(type = "SUBCLASS_OF" ,direction = Relationship.OUTGOING)
    private Set<OntologyTerm> subclassOf;

    @Relationship(type = "MAPPED_TO", direction = Relationship.INCOMING)
    private SampleToOntologyRelationship sampleMappedTo;

    @Relationship(type = "MAPPED_TO", direction = Relationship.INCOMING)
    private TreatmentToOntologyRelationship treatmentMappedTo;


    public OntologyTerm() {
    }

    public OntologyTerm(String url, String label) {
        this.url = url;
        this.label = label;
        this.directMappedSamplesNumber = 0;
        this.indirectMappedSamplesNumber = 0;
        this.synonyms = new HashSet<>();
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

    public SampleToOntologyRelationship getSampleMappedTo() {
        return sampleMappedTo;
    }

    public void setSampleMappedTo(SampleToOntologyRelationship sampleMappedTo) {
        this.sampleMappedTo = sampleMappedTo;
    }

    public void addSubclass(OntologyTerm ot){
        if(this.subclassOf == null){
            this.subclassOf = new HashSet<OntologyTerm>();
        }
        this.subclassOf.add(ot);
    }

    public int getDirectMappedSamplesNumber() {
        return directMappedSamplesNumber;
    }

    public void setDirectMappedSamplesNumber(int directMappedSamplesNumber) {

        this.directMappedSamplesNumber = directMappedSamplesNumber;
    }

    public int getIndirectMappedSamplesNumber() {
        return indirectMappedSamplesNumber;
    }

    public void setIndirectMappedSamplesNumber(int indirectMappedSamplesNumber) {
        this.indirectMappedSamplesNumber = indirectMappedSamplesNumber;
    }

    public Set<String> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(Set<String> synonyms) {
        this.synonyms = synonyms;
    }

    public TreatmentToOntologyRelationship getTreatmentMappedTo() {
        return treatmentMappedTo;
    }

    public void setTreatmentMappedTo(TreatmentToOntologyRelationship treatmentMappedTo) {
        this.treatmentMappedTo = treatmentMappedTo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isAllowAsSuggestion() {
        return allowAsSuggestion;
    }

    public void setAllowAsSuggestion(boolean allowAsSuggestion) {
        this.allowAsSuggestion = allowAsSuggestion;
    }
}
