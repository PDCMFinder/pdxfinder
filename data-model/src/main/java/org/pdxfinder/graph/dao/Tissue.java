package org.pdxfinder.graph.dao;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.neo4j.ogm.annotation.NodeEntity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Tissue represents a human tissue associated with a tumor
 */
@NodeEntity
public class Tissue {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    public Tissue() {
        // Empty constructor required as of Neo4j API 2.0.5
    }

    public Tissue(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Tissue tissue = (Tissue) o;

        return new EqualsBuilder()
            .append(getName(), tissue.getName())
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(getName())
            .toHashCode();
    }
}
