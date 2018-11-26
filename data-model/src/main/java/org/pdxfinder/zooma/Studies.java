package org.pdxfinder.zooma;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "study",
        "studyUri"
})
public class Studies {

    @JsonProperty("study")
    private String study;
    @JsonProperty("studyUri")
    private Object studyUri;


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
