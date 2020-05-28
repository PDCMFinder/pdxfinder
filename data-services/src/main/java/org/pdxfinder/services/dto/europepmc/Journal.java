package org.pdxfinder.services.dto.europepmc;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Journal {

    private String title;
    private String medlineAbbreviation;
    private String isoabbreviation;
    private String issn;
    private String nlmid;

    public Journal() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMedlineAbbreviation() {
        return medlineAbbreviation;
    }

    public void setMedlineAbbreviation(String medlineAbbreviation) {
        this.medlineAbbreviation = medlineAbbreviation;
    }

    public String getIsoabbreviation() {
        return isoabbreviation;
    }

    public void setIsoabbreviation(String isoabbreviation) {
        this.isoabbreviation = isoabbreviation;
    }

    public String getIssn() {
        return issn;
    }

    public void setIssn(String issn) {
        this.issn = issn;
    }

    public String getNlmid() {
        return nlmid;
    }

    public void setNlmid(String nlmid) {
        this.nlmid = nlmid;
    }
}
