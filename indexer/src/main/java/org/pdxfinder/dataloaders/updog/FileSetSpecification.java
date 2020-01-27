package org.pdxfinder.dataloaders.updog;

import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FileSetSpecification {

    public List<String> getRequiredFileList() {
        return requiredFileList;
    }

    private List<String> requiredFileList;

    private FileSetSpecification() {
        this.requiredFileList = new ArrayList();
    }

    public static FileSetSpecification create() {
        return new FileSetSpecification();
    }

    public final FileSetSpecification build() {
        return this;
    }

    public FileSetSpecification addRequiredFileList(List requiredFileList) {
        this.requiredFileList.addAll(requiredFileList);
        return this;
    }

    public List<String> getMissingFilesFrom(Map<String, Table> fileList) {
        List<String> missingFiles = requiredFileList;
        missingFiles.removeAll(fileList.keySet());
        return missingFiles;
    }


}
