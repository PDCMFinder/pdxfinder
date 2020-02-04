package org.pdxfinder.dataloaders.updog;

import org.apache.commons.lang3.tuple.Pair;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TableSetSpecification {

    private HashSet<String> requiredTables;
    private Map<String, ColumnSpecification> columnSpecification;
    private List<Pair<String, String>> nonEmptyColumns;
    private List<Pair<String, String>> uniqueColumns;
    private List<Pair<Pair<String, String>, Pair<String, String>>> oneToManyRelations;
    private String provider = "Not Specified";

    private TableSetSpecification() {
        this.requiredTables = new HashSet<>();
        this.nonEmptyColumns = new ArrayList<>();
        this.uniqueColumns = new ArrayList<>();
        this.oneToManyRelations = new ArrayList<>();
    }

    public static TableSetSpecification create() {
        return new TableSetSpecification();
    }

    public TableSetSpecification addHasRelations(
        Pair<String, String> leftTable,
        Pair<String, String> rightTable
    ) {
        this.oneToManyRelations.add(Pair.of(leftTable, rightTable));
        return this;
    }

    public TableSetSpecification addHasRelations(
        List<Pair<Pair<String, String>, Pair<String, String>>> relation
    ) {
        this.oneToManyRelations.addAll(relation);
        return this;
    }

    public TableSetSpecification addRequiredTables(Set<String> requiredTables) {
        this.requiredTables.addAll(requiredTables);
        return this;
    }

    public TableSetSpecification addRequiredColumnSets(Map<String, ColumnSpecification> columnSpecification) {
        this.columnSpecification = columnSpecification;
        return this;
    }

    public TableSetSpecification addRequiredColumns(Pair<String, String> tableColumn) {
        this.nonEmptyColumns.add(tableColumn);
        return this;
    }

    public TableSetSpecification addRequiredColumns(List<Pair<String, String>> tableColumns) {
        this.nonEmptyColumns.addAll(tableColumns);
        return this;
    }

    public TableSetSpecification addUniqueColumns(Pair<String, String> tableColumn) {
        this.uniqueColumns.add(tableColumn);
        return this;
    }

    public TableSetSpecification addUniqueColumns(List<Pair<String, String>> tableColumns) {
        this.uniqueColumns.addAll(tableColumns);
        return this;
    }

    public Map<String, ColumnSpecification> getColumnSpecification() {
        return this.columnSpecification;
    }

    public String getProvider() {
        return provider;
    }

    public TableSetSpecification setProvider(String provider) {
        this.provider = provider;
        return this;
    }

    public List<Pair<String, String>> getNonEmptyColumns() {
        return this.nonEmptyColumns;
    }

    public List<Pair<String, String>> getUniqueColumns() {
        return this.uniqueColumns;
    }

    public boolean hasRequiredColumns() {
        return (columnSpecification != null);
    }

    public Set<String> getRequiredTables() {
        return requiredTables;
    }

    public List<Pair<Pair<String, String>, Pair<String, String>>> getOneToManyRelations() {
        return oneToManyRelations;
    }

    public Set<String> getMissingTablesFrom(Map<String, Table> fileList) {
        Set<String> missingFiles = requiredTables;
        missingFiles.removeAll(fileList.keySet());
        return missingFiles;
    }


}
