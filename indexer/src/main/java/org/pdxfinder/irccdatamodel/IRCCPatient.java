package org.pdxfinder.irccdatamodel;

/**
 * Created by csaba on 21/04/2017.
 */
public class IRCCPatient {

    String externalId;
    String sex;
    String ethnicity;
    String race;
    String primarySite;
    String grade;

    public IRCCPatient(String externalId, String sex, String ethnicity, String race, String primarySite, String grade) {
        this.externalId = externalId;
        this.sex = sex;
        this.ethnicity = ethnicity;
        this.race = race;
        this.primarySite = primarySite;
        this.grade = grade;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getEthnicity() {
        return ethnicity;
    }

    public void setEthnicity(String ethnicity) {
        this.ethnicity = ethnicity;
    }

    public String getRace() {
        return race;
    }

    public void setRace(String race) {
        this.race = race;
    }

    public String getPrimarySite() {
        return primarySite;
    }

    public void setPrimarySite(String primarySite) {
        this.primarySite = primarySite;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }
}
