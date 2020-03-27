package org.pdxfinder.commandline;

import org.pdxfinder.utils.ExportDataToTemplate;;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class FinderTransformer {

    @Value("${data-dir}") private String defaultDirectory;
    private ExportDataToTemplate exportDataToTemplate;
    private File rootDir;

    @Autowired
    FinderTransformer(ExportDataToTemplate exportDataToTemplate){
       this.exportDataToTemplate = exportDataToTemplate;
    }

    void run(File dataDirectory, String provider, boolean loadAll) throws IOException {
        resolveRootDir(dataDirectory);
        if (loadAll){
            exportDataToTemplate.exportAllGroups(rootDir);
        }
        else
           exportDataToTemplate.export(rootDir, provider);
    }

    private void resolveRootDir(File dataDirectory) throws IOException{
        if(dataDirectory != null && dataDirectory.exists()){
            rootDir = dataDirectory;
        } else if (!(defaultDirectory == null || defaultDirectory.isEmpty())) {
            rootDir = new File(defaultDirectory);
        } else throw new IOException("Root directory does not exist");
    }

    public void setDefaultDirectory(String defaultDirectory) {
        this.defaultDirectory = defaultDirectory;
    }
}
