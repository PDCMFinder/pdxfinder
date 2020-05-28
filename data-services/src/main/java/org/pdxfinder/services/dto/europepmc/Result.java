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


    private String pmid;
    private String title;
    private String authorString;
    private JournalInfo journalInfo;

    private String pubYear;
    private String abstractText;

    public Result() {
    }

    public Result(String pmid, String title,
                  String authorString, JournalInfo journalInfo,
                  String pubYear, String abstractText) {
        this.pmid = pmid;
        this.title = title;
        this.authorString = authorString;
        this.journalInfo = journalInfo;
        this.pubYear = pubYear;
        this.abstractText = abstractText;
    }

    public String getPmid() {
        return pmid;
    }

    public void setPmid(String pmid) {
        this.pmid = pmid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthorString(String authorString) {
        this.authorString = authorString;
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

    public JournalInfo getJournalInfo() {
        return journalInfo;
    }

    public void setJournalInfo(JournalInfo journalInfo) {
        this.journalInfo = journalInfo;
    }

    public void setPubYear(String pubYear) {
        this.pubYear = pubYear;
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
}
