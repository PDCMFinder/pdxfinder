package org.pdxfinder.dataloaders.updog;

import org.apache.commons.lang3.tuple.Pair;
import tech.tablesaw.api.Table;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

public class TableSetSpecification {

    private Set<String> requiredTables;
    private Set<Pair<String, String>> requiredColumns;
    private Set<Pair<String, String>> nonEmptyColumns;
    private Set<Pair<String, String>> uniqueColumns;
    private Set<Pair<Pair<String, String>, Pair<String, String>>> relations;
    private String provider = "Not Specified";

    private TableSetSpecification() {
        this.requiredTables = new HashSet<>();
        this.requiredColumns = new HashSet<>();
        this.nonEmptyColumns = new HashSet<>();
        this.uniqueColumns = new HashSet<>();
        this.relations = new HashSet<>();
    }

    public static TableSetSpecification create() {
        return new TableSetSpecification();
    }

    public TableSetSpecification addHasRelations(
        Pair<String, String> leftTable,
        Pair<String, String> rightTable
    ) {
        this.relations.add(Pair.of(leftTable, rightTable));
        return this;
    }

    public TableSetSpecification addHasRelations(
        Set<Pair<Pair<String, String>, Pair<String, String>>> relation
    ) {
        this.relations.addAll(relation);
        return this;
    }

    public TableSetSpecification addRequiredTables(Set<String> requiredTables) {
        this.requiredTables.addAll(requiredTables);
        return this;
    }

    public TableSetSpecification addRequiredColumns(Pair<String, String> tableColumn) {
        this.requiredColumns.add(tableColumn);
        return this;
    }

    public TableSetSpecification addRequiredColumns(Set<Pair<String, String>> tableColumn) {
        this.requiredColumns.addAll(tableColumn);
        return this;
    }

    public TableSetSpecification addNonEmptyColumns(Pair<String, String> tableColumn) {
        this.nonEmptyColumns.add(tableColumn);
        return this;
    }

    public TableSetSpecification addNonEmptyColumns(Set<Pair<String, String>> tableColumns) {
        this.nonEmptyColumns.addAll(tableColumns);
        return this;
    }

    public TableSetSpecification addUniqueColumns(Pair<String, String> tableColumn) {
        this.uniqueColumns.add(tableColumn);
        return this;
    }

    public TableSetSpecification addUniqueColumns(Set<Pair<String, String>> tableColumns) {
        this.uniqueColumns.addAll(tableColumns);
        return this;
    }

    public String getProvider() {
        return provider;
    }

    public TableSetSpecification setProvider(String provider) {
        this.provider = provider;
        return this;
    }

    public Set<Pair<String, String>> getNonEmptyColumns() {
        return this.nonEmptyColumns;
    }

    public Set<Pair<String, String>> getUniqueColumns() {
        return this.uniqueColumns;
    }

    public Set<String> getRequiredTables() {
        return this.requiredTables;
    }

    public Set<Pair<String, String>> getRequiredColumns() {
        return this.requiredColumns;
    }

    public boolean hasRequiredColumns() {
        return getRequiredColumns() != null;
    }

    public Set<Pair<Pair<String, String>, Pair<String, String>>> getHasRelations() {
        return this.relations;
    }

    public Set<String> getMissingTablesFrom(Map<String, Table> fileList) {
        Set<String> missingFiles = requiredTables;
        missingFiles.removeAll(fileList.keySet());
        return missingFiles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TableSetSpecification that = (TableSetSpecification) o;

        if (getRequiredTables() != null
            ? !getRequiredTables().equals(that.getRequiredTables())
            : that.getRequiredTables() != null)
            return false;
        if (getRequiredColumns() != null
            ? !getRequiredColumns().equals(that.getRequiredColumns())
            : that.getRequiredColumns() != null)
            return false;
        if (getNonEmptyColumns() != null
            ? !getNonEmptyColumns().equals(that.getNonEmptyColumns())
            : that.getNonEmptyColumns() != null)
            return false;
        if (getUniqueColumns() != null
            ? !getUniqueColumns().equals(that.getUniqueColumns())
            : that.getUniqueColumns() != null)
            return false;
        if (getHasRelations() != null
            ? !getHasRelations().equals(that.getHasRelations())
            : that.getHasRelations() != null)
            return false;
        return getProvider().equals(that.getProvider());
    }

    @Override
    public int hashCode() {
        int result = getRequiredTables() != null ? getRequiredTables().hashCode() : 0;
        result = 31 * result + (getRequiredColumns() != null ? getRequiredColumns().hashCode() : 0);
        result = 31 * result + (getNonEmptyColumns() != null ? getNonEmptyColumns().hashCode() : 0);
        result = 31 * result + (getUniqueColumns() != null ? getUniqueColumns().hashCode() : 0);
        result = 31 * result + (getHasRelations() != null ? getHasRelations().hashCode() : 0);
        result = 31 * result + getProvider().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", TableSetSpecification.class.getSimpleName() + "[", "]")
            .add("requiredTables=" + requiredTables)
            .add("requiredColumns=" + requiredColumns)
            .add("nonEmptyColumns=" + nonEmptyColumns)
            .add("uniqueColumns=" + uniqueColumns)
            .add("relations=" + relations)
            .add("provider='" + provider + "'")
            .toString();
    }
}
