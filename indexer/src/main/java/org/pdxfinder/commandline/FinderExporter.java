package org.pdxfinder.commandline;

import org.apache.commons.lang3.StringUtils;
import org.pdxfinder.dataexport.ExportSheets;
import org.pdxfinder.dataexport.UniversalDataExporter;
import org.pdxfinder.dataexport.UniversalDataExtractor;
import org.pdxfinder.graph.dao.Group;
import org.pdxfinder.services.DataImportService;
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

    @Value("${data-dir}")
    private String defaultDirectory;
    private File rootDir;
    private static final Logger log = LoggerFactory.getLogger(FinderExporter.class);

    private DataImportService dataImportService;
    private UniversalDataExtractor universalDataExtractor;
    private UniversalDataExporter universalDataExporter;

    @Autowired
    FinderExporter(DataImportService dataImportService, UniversalDataExtractor universalDataExtractor, UniversalDataExporter universalDataExporter){
        this.dataImportService = dataImportService;
        this.universalDataExtractor = universalDataExtractor;
        this.universalDataExporter = universalDataExporter;
    }
    public void run(File dataDirectory, String provider, boolean loadAll, boolean isHarmonized) throws IOException {
        resolveRootDir(dataDirectory);
        if(loadAll){
            exportAllGroups(rootDir, isHarmonized);
        }
        else if (StringUtils.isNotEmpty(provider)) {
            export(rootDir, provider, isHarmonized);
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

    public void exportAllGroups(File rootDir, boolean isUnharmonized){
        List<Group> allProviders = dataImportService.getAllProviderGroups();
        allProviders.forEach(g -> {
            try {
                export(rootDir, g.getAbbreviation(),isUnharmonized);
            } catch (IOException e) {
                log.error(Arrays.toString(e.getStackTrace()));
            }
        });
    }

    public void export(File rootDir, String dataSourceAbbrev, boolean unharmonized) throws IOException {
        Group ds = dataImportService.findProviderGroupByAbbrev(dataSourceAbbrev);
        if(ds == null) {
            log.error("Datasource {} not found. ",dataSourceAbbrev);
            return;
        }
        ExportSheets xDogSheets = new ExportSheets();
        xDogSheets.init(rootDir + "/template", ds);
        universalDataExtractor.extract(xDogSheets, unharmonized);
        universalDataExporter.export(rootDir + "/export", xDogSheets);
    }

    public void setDefaultDirectory(String defaultDirectory) {
        this.defaultDirectory = defaultDirectory;
    }
}
