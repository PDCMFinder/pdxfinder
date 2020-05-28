package org.pdxfinder.services.dto.europepmc;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JournalInfo {

    private String issue;
    private String volume;
    private Integer journalIssueId;
    private String dateOfPublication;
    private Integer monthOfPublication;
    private Integer yearOfPublication;
    private String printPublicationDate;
    private Journal journal;

    public JournalInfo() {
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public Integer getJournalIssueId() {
        return journalIssueId;
    }

    public void setJournalIssueId(Integer journalIssueId) {
        this.journalIssueId = journalIssueId;
    }

    public String getDateOfPublication() {
        return dateOfPublication;
    }

    public void setDateOfPublication(String dateOfPublication) {
        this.dateOfPublication = dateOfPublication;
    }

    public Integer getMonthOfPublication() {
        return monthOfPublication;
    }

    public void setMonthOfPublication(Integer monthOfPublication) {
        this.monthOfPublication = monthOfPublication;
    }

    public Integer getYearOfPublication() {
        return yearOfPublication;
    }

    public void setYearOfPublication(Integer yearOfPublication) {
        this.yearOfPublication = yearOfPublication;
    }

    public String getPrintPublicationDate() {
        return printPublicationDate;
    }

    public void setPrintPublicationDate(String printPublicationDate) {
        this.printPublicationDate = printPublicationDate;
    }

    public Journal getJournal() {
        return journal;
    }

    public void setJournal(Journal journal) {
        this.journal = journal;
    }
}
