package org.pdxfinder.admin.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/*
 * Created by csaba on 18/06/2018.
 */
public class MappingEntity {

    /**
     * A Long number identifying the entity. This id is used for referring the entity.
     */
    private Long entityId;


    /**
     * Describes what kind of mapping info is held in the entity, ie: diagnosis, drug, etc
     */
    private String mapType;


    /**
     * A list of attributes that are used to map objects.
     * IE: ["diagnosis", "source", "primaryTissue", "tumorType"]
     * The elements of this list is used as headers when listing the mapping entities as well as keys to the mappingValues
     */
    private List<String> mappingLabels;


    /**
     * The corresponding values for the mapping labels
     * IE: ["diagnosis"=>"Carcinoma", "source"=>"JAX"]
     */
    private Map<String, String> mappingValues;


    /**
     * The term that the entity is mapped to. The value of this attribute should be either null (indicating that the
     * entity is not mapped to anything yet) or an existing ontology term.
     */
    private String mappedTerm;


    /**
     * Possible values:
     * Created : temporary status when the entity is mapped to a term but editing is possible
     * Mapped : the entity is mapped to a term but the mapping is not yet verified by the institute
     * Verified : the entity is mapped to a term and the mapping is verified by the institute
     */
    private String status;


    /**
     * A list of entities that are similar to the current entity. This list is empty if the entity's mappedTerm is not null.
     */
    private List<MappingEntity> suggestedMappings;


    /**
     * The date when the entity was created.
     */
    private Date dateCreated;


    /**
     * The date when the entity was last updated.
     */
    private Date dateUpdated;


    public MappingEntity(Long entityId, String mapType, List<String> mappingLabels, Map<String, String> mappingValues) {
        this.entityId = entityId;
        this.mapType = mapType;
        this.mappingLabels = mappingLabels;
        this.mappingValues = mappingValues;
        this.mappedTerm = null;
        this.status = "Created";
        this.suggestedMappings = new ArrayList<>();
        //TODO: get current date, specify date format
        this.dateCreated = null;
        this.dateUpdated = null;

    }

    public MappingEntity(Long entityId, String mapType, List<String> mappingLabels, Map<String, String> mappingValues,
                         String mappedTerm, String status, List<MappingEntity> suggestedMappings, Date dateCreated,
                         Date dateUpdated) {

        this.entityId = entityId;
        this.mapType = mapType;
        this.mappingLabels = mappingLabels;
        this.mappingValues = mappingValues;
        this.mappedTerm = mappedTerm;
        this.status = status;
        this.suggestedMappings = suggestedMappings;
        this.dateCreated = dateCreated;
        this.dateUpdated = dateUpdated;
    }


    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getMapType() {
        return mapType;
    }

    public void setMapType(String mapType) {
        this.mapType = mapType;
    }

    public List<String> getMappingLabels() {
        return mappingLabels;
    }

    public void setMappingLabels(List<String> mappingLabels) {
        this.mappingLabels = mappingLabels;
    }

    public Map<String, String> getMappingValues() {
        return mappingValues;
    }

    public void setMappingValues(Map<String, String> mappingValues) {
        this.mappingValues = mappingValues;
    }

    public String getMappedTerm() {
        return mappedTerm;
    }

    public void setMappedTerm(String mappedTerm) {
        this.mappedTerm = mappedTerm;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<MappingEntity> getSuggestedMappings() {
        return suggestedMappings;
    }

    public void setSuggestedMappings(List<MappingEntity> suggestedMappings) {
        this.suggestedMappings = suggestedMappings;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }
}
