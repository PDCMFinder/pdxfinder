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
import java.util.Arrays;
import java.util.List;

@Component
public class FinderExporter {

    UniversalDataExporter downDog;
    private UtilityService utilityService;
    private DataImportService dataImportService;

    @Autowired
    FinderExporter(UtilityService utilityService, DataImportService dataImportService, UniversalDataExporter universalDataExporter){
        this.utilityService = utilityService;
        this.dataImportService = dataImportService;
        this.downDog = universalDataExporter;
    }

    @Value("${data-dir}")
    private String defaultDirectory;
    private File rootDir;
    private static final Logger log = LoggerFactory.getLogger(FinderExporter.class);

    public void run(File dataDirectory,String provider,boolean loadAll) throws IOException {
        resolveRootDir(dataDirectory);
        if(loadAll){
            exportAllGroups(rootDir);
        }
        else if (provider != null && !provider.isEmpty()) {
            export(rootDir, provider);
        }
    }

    public void resolveRootDir(File dataDirectory) throws IOException {
        if (dataDirectory != null && dataDirectory.exists()){
            rootDir = dataDirectory;
        } else if (defaultDirectory != null) {
            rootDir = new File (defaultDirectory);
        } else if (!rootDir.exists()) {
            throw new IOException("Cannot resolve root data directory");
        }
    }

    public void exportAllGroups(File rootDir){
        List<Group> allProviders = dataImportService.getAllProviderGroups();
        allProviders.forEach(g -> {
            try {
                export(rootDir, g.getAbbreviation());
            } catch (IOException e) {
                log.error(Arrays.toString(e.getStackTrace()));
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

    public void setDefaultDirectory(String defaultDirectory) {
        this.defaultDirectory = defaultDirectory;
    }
}
