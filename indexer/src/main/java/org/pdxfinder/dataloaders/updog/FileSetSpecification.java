package org.pdxfinder.dataloaders.updog;

import org.apache.commons.lang3.tuple.Pair;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FileSetSpecification {

    private HashSet<String> requiredFileList;
    private Map<String, ColumnSpecification> columnSpecification;
    private List<Pair<String, String>> requiredColumns;

    private FileSetSpecification() {
        this.requiredFileList = new HashSet<>();
        this.requiredColumns = new ArrayList<>();
    }

    public static FileSetSpecification create() {
        return new FileSetSpecification();
    }

    public final FileSetSpecification build() {
        return this;
    }

    public FileSetSpecification addRequiredFileList(Set<String> requiredFileList) {
        this.requiredFileList.addAll(requiredFileList);
        return this;
    }

    public FileSetSpecification addRequiredColumnSets(Map<String, ColumnSpecification> columnSpecification) {
        this.columnSpecification = columnSpecification;
        return this;
    }

    public FileSetSpecification addRequiredColumns(Pair<String, String> tableColumn) {
        this.requiredColumns.add(tableColumn);
        return this;
    }

    public FileSetSpecification addRequiredColumns(List<Pair<String, String>> tableColumns) {
        this.requiredColumns.addAll(tableColumns);
        return this;
    }

    public Map<String, ColumnSpecification> getColumnSpecification() {
        return this.columnSpecification;
    }

    public List<Pair<String, String>> getRequiredColumns() {
        return this.requiredColumns;
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
