package org.pdxfinder.zooma;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "bioEntity",
        "studies",
        "bioEntityUri"
})
public class BiologicalEntities {

    private String bioEntity;
    private Studies studies;
    private Object bioEntityUri;


    public BiologicalEntities() {
    }

    public BiologicalEntities(String bioEntity, Studies studies, Object bioEntityUri) {
        this.bioEntity = bioEntity;
        this.studies = studies;
        this.bioEntityUri = bioEntityUri;
    }

    @JsonProperty("bioEntity")
    public String getBioEntity() {
        return bioEntity;
    }

    @JsonProperty("bioEntity")
    public void setBioEntity(String bioEntity) {
        this.bioEntity = bioEntity;
    }

    @JsonProperty("studies")
    public Studies getStudies() {
        return studies;
    }

    @JsonProperty("studies")
    public void setStudies(Studies studies) {
        this.studies = studies;
    }

    @JsonProperty("bioEntityUri")
    public Object getBioEntityUri() {
        return bioEntityUri;
    }

    @JsonProperty("bioEntityUri")
    public void setBioEntityUri(Object bioEntityUri) {
        this.bioEntityUri = bioEntityUri;
    }

}