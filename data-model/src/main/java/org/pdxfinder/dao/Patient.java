
package org.pdxfinder.dao;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@NodeEntity
public class Patient {

    @GraphId
    private Long id;

    private String externalId;
    private String sex;
    private String age;
    private String race;
    private String ethnicity;
    private String dataSource;


    private Patient() {
        // Empty constructor required as of Neo4j API 2.0.5
    }

    public Patient(String externalId, String sex, String age, String race, String ethnicity, ExternalDataSource externalDataSource) {
        this.externalId = externalId;
        this.sex = sex;
        this.age = age;
        this.race = race;
        this.ethnicity = ethnicity;
        this.dataSource = externalDataSource.getAbbreviation();
    }

    @Relationship(type = "TUMOR-SOURCE", direction = Relationship.INCOMING)
    private Set<Tumor> tumors;

    public void hasTumor(Tumor tumor) {
        if (tumors == null) {
            tumors = new HashSet<>();
        }
        tumors.add(tumor);
    }

    public String toString() {

        return Optional.ofNullable(this.age).orElse("").concat(" y.o. ") + Optional.of(" " + this.sex).orElse("") + ", Tumors => "
                + Optional.ofNullable(this.tumors).orElse(
                Collections.emptySet()).stream().map(
                Tumor::getType).collect(Collectors.toList());
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

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
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

    public Set<Tumor> getTumors() {
        return tumors;
    }

    public void setTumors(Set<Tumor> tumors) {
        this.tumors = tumors;
    }

}

