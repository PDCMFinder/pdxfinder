package org.pdxfinder.services.dto.europepmc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "source",
        "pmid",
        "doi",
        "title",
        "authorString",
        "journalTitle",
        "issue",
        "journalVolume",
        "pubYear",
        "abstractText",
        "journalIssn",
        "pageInfo",
        "pubType",
        "isOpenAccess",
        "inEPMC",
        "inPMC",
        "hasPDF",
        "hasBook",
        "hasSuppl",
        "citedByCount",
        "hasReferences",
        "hasTextMinedTerms",
        "hasDbCrossReferences",
        "hasLabsLinks",
        "hasTMAccessionNumbers",
        "firstIndexDate",
        "firstPublicationDate"
})
public class Result {

    private String id;
    private String source;
    private String pmid;
    private String doi;
    private String title;
    private String authorString;
    private String journalTitle;
    private String issue;
    private String journalVolume;
    private String pubYear;
    private String abstractText;
    private String journalIssn;
    private String pageInfo;
    private String pubType;
    private String isOpenAccess;
    private String inEPMC;
    private String inPMC;
    private String hasPDF;
    private String hasBook;
    private String hasSuppl;
    private Integer citedByCount;
    private String hasReferences;
    private String hasTextMinedTerms;
    private String hasDbCrossReferences;
    private String hasLabsLinks;
    private String hasTMAccessionNumbers;
    private String firstIndexDate;
    private String firstPublicationDate;

    public Result() {
    }

    public Result(String title, String authorString, String journalTitle, String pubYear) {
        this.title = title;
        this.authorString = authorString;
        this.journalTitle = journalTitle;
        this.pubYear = pubYear;
    }

    public String getId() {
        return id;
    }

    public String getSource() {
        return source;
    }

    public String getPmid() {
        return pmid;
    }

    public String getDoi() {
        return doi;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthorString() {

        String delimiter = ", ";
        String first3Authors = Arrays.stream(this.authorString.split(delimiter))
                .limit(3)
                .map(Object::toString)
                .collect(Collectors.joining(delimiter));

        String lastAuthor = this.authorString.substring(this.authorString.lastIndexOf(delimiter) + 1);

        return String.format("%s ... %s", first3Authors, lastAuthor);
    }

    public String getJournalTitle() {
        return journalTitle;
    }

    public String getIssue() {
        return issue;
    }

    public String getJournalVolume() {
        return journalVolume;
    }

    public String getPubYear() {
        return pubYear;
    }

    public String getAbstractText() {
        return abstractText;
    }

    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }

    public String getJournalIssn() {
        return journalIssn;
    }

    public String getPageInfo() {
        return pageInfo;
    }

    public String getPubType() {
        return pubType;
    }

    public String getIsOpenAccess() {
        return isOpenAccess;
    }

    public String getInEPMC() {
        return inEPMC;
    }

    public String getInPMC() {
        return inPMC;
    }

    public String getHasPDF() {
        return hasPDF;
    }

    public String getHasBook() {
        return hasBook;
    }

    public String getHasSuppl() {
        return hasSuppl;
    }

    public Integer getCitedByCount() {
        return citedByCount;
    }

    public String getHasReferences() {
        return hasReferences;
    }

    public String getHasTextMinedTerms() {
        return hasTextMinedTerms;
    }

    public String getHasDbCrossReferences() {
        return hasDbCrossReferences;
    }

    public String getHasLabsLinks() {
        return hasLabsLinks;
    }

    public String getHasTMAccessionNumbers() {
        return hasTMAccessionNumbers;
    }

    public String getFirstIndexDate() {
        return firstIndexDate;
    }

    public String getFirstPublicationDate() {
        return firstPublicationDate;
    }
}