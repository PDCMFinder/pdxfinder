package org.pdxfinder.commandline;

import org.pdxfinder.dataexport.UniversalDataExporter;
import org.pdxfinder.graph.dao.Group;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
public class FinderTransformer {

    private static final Logger log = LoggerFactory.getLogger(FinderTransformer.class);

    @Value("${data-dir}") private String predefDataDirectory;
    private UtilityService utilityService;
    private DataImportService dataImportService;
    private File rootDir;

    @Autowired
    FinderTransformer(UtilityService utilityService, DataImportService dataImportService){
        this.utilityService = utilityService;
        this.dataImportService = dataImportService;
    }

    void run(File dataDirectory, String provider, boolean loadAll) throws IOException {
        resolveRootDir(dataDirectory);
        if (loadAll){
            exportAllGroups(rootDir);
        }
        else
           export(rootDir, provider);
    }

    private void resolveRootDir(File dataDirectory) throws IOException{
        if(dataDirectory != null && dataDirectory.exists()){
            rootDir = dataDirectory;
        } else if (!rootDir.exists()) {
            rootDir = new File(predefDataDirectory);
        }
        if(rootDir.exists()){
            throw new IOException("Root directory does not exist");
        }
    }

    public void exportAllGroups(File rootDir){
        List<Group> allProviders = dataImportService.getAllProviderGroups();
        allProviders.forEach(g -> {
            try {
                export(rootDir, g.getAbbreviation());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void export(File rootDir, String dataSourceAbbrev) throws IOException {
        Group ds = dataImportService.findProviderGroupByAbbrev(dataSourceAbbrev);
        if(ds == null) {
            log.error("Datasource {} not found. ",dataSourceAbbrev);
            return;
        }
        UniversalDataExporter downDog = new UniversalDataExporter(dataImportService, utilityService);
        downDog.init(rootDir + "/template", ds);
        downDog.export(rootDir + "/export");
    }

    public void setRootDir(File rootDir) {
        this.rootDir = rootDir;
    }
}
