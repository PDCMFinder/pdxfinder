package org.pdxfinder.admin.zooma;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({
        "source",
        "evidence",
        "accuracy",
        "annotator",
        "annotatedDate"
})
public class Provenance {

    private Source source;
    private String evidence;
    private String accuracy;
    private String annotator;
    private String annotatedDate;


    public Provenance() {
    }

    public Provenance(Source source, String evidence, String accuracy, String annotator, String annotatedDate) {
        this.source = source;
        this.evidence = evidence;
        this.accuracy = accuracy;
        this.annotator = annotator;
        this.annotatedDate = annotatedDate;
    }

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
