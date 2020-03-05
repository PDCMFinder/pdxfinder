package org.pdxfinder.graph.dao;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.neo4j.ogm.annotation.NodeEntity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/*
 * Created by csaba on 26/04/2018.
 */
@NodeEntity
public class EngraftmentMaterial {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String state;


    public EngraftmentMaterial() {
        // Empty constructor required as of Neo4j API 2.0.5
    }

    public EngraftmentMaterial(String name) {
        this.name = name;
    }

    public EngraftmentMaterial(String name, String state) {
        this.name = name;
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        EngraftmentMaterial that = (EngraftmentMaterial) o;

        return new EqualsBuilder()
            .append(getName(), that.getName())
            .append(getState(), that.getState())
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(getName())
            .append(getState())
            .toHashCode();
    }
}
