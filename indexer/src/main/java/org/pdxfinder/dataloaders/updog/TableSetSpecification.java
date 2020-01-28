package org.pdxfinder.dataloaders.updog;

import org.apache.commons.lang3.tuple.Pair;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TableSetSpecification {

    private HashSet<String> requiredFileList;
    private Map<String, ColumnSpecification> columnSpecification;
    private List<Pair<String, String>> requiredColumns;
    private List<Pair<String, String>> uniqueColumns;
    private String provider = "Not Specified";

    private TableSetSpecification() {
        this.requiredFileList = new HashSet<>();
        this.requiredColumns = new ArrayList<>();
        this.uniqueColumns = new ArrayList<>();
    }

    public static TableSetSpecification create() {
        return new TableSetSpecification();
    }

    public TableSetSpecification addRequiredFileList(Set<String> requiredFileList) {
        this.requiredFileList.addAll(requiredFileList);
        return this;
    }

    public TableSetSpecification addRequiredColumnSets(Map<String, ColumnSpecification> columnSpecification) {
        this.columnSpecification = columnSpecification;
        return this;
    }

    public TableSetSpecification addRequiredColumns(Pair<String, String> tableColumn) {
        this.requiredColumns.add(tableColumn);
        return this;
    }

    public TableSetSpecification addRequiredColumns(List<Pair<String, String>> tableColumns) {
        this.requiredColumns.addAll(tableColumns);
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

    public List<Pair<String, String>> getRequiredColumns() {
        return this.requiredColumns;
    }

    public List<Pair<String, String>> getUniqueColumns() {
        return this.uniqueColumns;
    }

    public boolean hasRequiredColumns() {
        return (columnSpecification != null);
    }

    public Set<String> getRequiredFileList() {
        return requiredFileList;
    }

    public Set<String> getMissingFilesFrom(Map<String, Table> fileList) {
        Set<String> missingFiles = requiredFileList;
        missingFiles.removeAll(fileList.keySet());
        return missingFiles;
    }


}
