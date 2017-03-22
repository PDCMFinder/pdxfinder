
package org.pdxfinder.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

@NodeEntity
public class Patient {

    @GraphId
    private Long id;

    private String externalId;
    private String sex;
    private String race;
    private String ethnicity;
    private String dataSource;
    private ExternalDataSource externalDataSource;

    private Patient() {
        // Empty constructor required as of Neo4j API 2.0.5
    }

    public Patient(String externalId, String sex, String race, String ethnicity, ExternalDataSource externalDataSource) {
        this.externalId = externalId;
        this.sex = sex;
        this.race = race;
        this.ethnicity = ethnicity;
        this.dataSource = externalDataSource.getAbbreviation();
        this.externalDataSource = externalDataSource;
    }

    @Relationship(type = "COLLECTION_EVENT", direction = Relationship.OUTGOING)
    private Set<PatientSnapshot> snapshots;

    public void hasSnapshot(PatientSnapshot snapshot) {
        if (snapshots == null) {
            snapshots = new HashSet<>();
        }
        snapshots.add(snapshot);
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

    public String getRace() {
        return race;
    }

    public void setRace(String race) {
        this.race = race;
    }

    public String getEthnicity() {
        return ethnicity;
    }

    public void setEthnicity(String ethnicity) {
        this.ethnicity = ethnicity;
    }

    public Set<PatientSnapshot> getSnapshots() {
        return snapshots;
    }

    public void setSnapshots(Set<PatientSnapshot> snapshots) {
        this.snapshots = snapshots;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public ExternalDataSource getExternalDataSource() {
        return externalDataSource;
    }

    public void setExternalDataSource(ExternalDataSource externalDataSource) {
        this.externalDataSource = externalDataSource;
    }
}

