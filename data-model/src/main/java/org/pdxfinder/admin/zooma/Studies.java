package org.pdxfinder.admin.zooma;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "study",
        "studyUri"
})
public class Studies {

    private String study;
    private Object studyUri;


    public Studies() {
    }

    public Studies(String study, Object studyUri) {
        this.study = study;
        this.studyUri = studyUri;
    }

    @JsonProperty("study")
    public String getStudy() {
        return study;
    }

    @JsonProperty("study")
    public void setStudy(String study) {
        this.study = study;
    }

    @JsonProperty("studyUri")
    public Object getStudyUri() {
        return studyUri;
    }

    @JsonProperty("studyUri")
    public void setStudyUri(Object studyUri) {
        this.studyUri = studyUri;
    }

}

