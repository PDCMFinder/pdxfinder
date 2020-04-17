package org.pdxfinder.dataloaders.updog.validation;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Relation {
    private String leftTableName;
    private String leftColumnName;
    private String rightTableName;
    private String rightColumnName;

    private Relation(
        String leftTableName,
        String leftColumnName,
        String rightTableName,
        String rightColumnName
    ) {
        this.leftTableName = leftTableName;
        this.leftColumnName = leftColumnName;
        this.rightTableName = rightTableName;
        this.rightColumnName = rightColumnName;
    }

    public static Relation between(ColumnReference left, ColumnReference right) {
        return new Relation(left.table(), left.column(), right.table(), right.column());
    }

    String leftTable() {
        return this.leftTableName;
    }

    String leftColumn() {
        return this.leftColumnName;
    }

    String rightTable() {
        return this.rightTableName;
    }

    String rightColumn() {
        return this.rightColumnName;
    }

    ColumnReference leftColumnReference() {
        return ColumnReference.of(leftTable(), leftColumn());
    }

    ColumnReference rightColumnReference() {
        return ColumnReference.of(rightTable(), rightColumn());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Relation relation = (Relation) o;

        return new EqualsBuilder()
            .append(leftTableName, relation.leftTableName)
            .append(leftColumnName, relation.leftColumnName)
            .append(rightTableName, relation.rightTableName)
            .append(rightColumnName, relation.rightColumnName)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(leftTableName)
            .append(leftColumnName)
            .append(rightTableName)
            .append(rightColumnName)
            .toHashCode();
    }

    @Override
    public String toString() {
        return String.format(
            "(%s) %s -> %s (%s)",
            leftTableName,
            leftColumnName,
            rightColumnName,
            rightTableName);
    }

}
