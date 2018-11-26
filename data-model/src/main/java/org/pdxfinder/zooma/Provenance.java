package org.pdxfinder.zooma;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "source",
        "evidence",
        "accuracy",
        "annotator",
        "annotatedDate"
})
public class Provenance {

    @JsonProperty("source")
    private Source source;
    @JsonProperty("evidence")
    private String evidence;
    @JsonProperty("accuracy")
    private String accuracy;
    @JsonProperty("annotator")
    private String annotator;
    @JsonProperty("annotatedDate")
    private String annotatedDate;

    @JsonProperty("source")
    public Source getSource() {
        return source;
    }

    @JsonProperty("source")
    public void setSource(Source source) {
        this.source = source;
    }

    @JsonProperty("evidence")
    public String getEvidence() {
        return evidence;
    }

    @JsonProperty("evidence")
    public void setEvidence(String evidence) {
        this.evidence = evidence;
    }

    @JsonProperty("accuracy")
    public String getAccuracy() {
        return accuracy;
    }

    @JsonProperty("accuracy")
    public void setAccuracy(String accuracy) {
        this.accuracy = accuracy;
    }

    @JsonProperty("annotator")
    public String getAnnotator() {
        return annotator;
    }

    @JsonProperty("annotator")
    public void setAnnotator(String annotator) {
        this.annotator = annotator;
    }

    @JsonProperty("annotatedDate")
    public String getAnnotatedDate() {
        return annotatedDate;
    }

    @JsonProperty("annotatedDate")
    public void setAnnotatedDate(String annotatedDate) {
        this.annotatedDate = annotatedDate;
    }

}
