package org.pdxfinder.dataloaders.updog;

import tech.tablesaw.api.Table;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FileSetSpecification {

    private HashSet<String> requiredFileList;
    private Map<String, ColumnSpecification> columnSpecification;

    private FileSetSpecification() {
        this.requiredFileList = new HashSet<>();
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

    public Map<String, ColumnSpecification> getColumnSpecification() {
        return this.columnSpecification;
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
