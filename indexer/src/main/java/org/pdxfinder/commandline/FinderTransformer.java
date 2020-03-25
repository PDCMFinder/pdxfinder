package org.pdxfinder.commandline;

import org.pdxfinder.utils.CbpTransformer;
import org.pdxfinder.utils.CbpTransformer.cbioType;
import org.pdxfinder.utils.ExportDataToTemplate;;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class FinderTransformer {

    private static final Logger log = LoggerFactory.getLogger(FinderTransformer.class);

    @Value("${data-dir}")
    private String defaultDirectory;
    private String defaultTemplateDirectory = defaultDirectory + "/template";
    private String defaultExportDirectory = defaultDirectory + "/export";
    private ExportDataToTemplate exportDataToTemplate;
    private CbpTransformer cbioTransformer;
    private File rootDir;
    private File templateDir;
    private File exportDir;
    private cbioType resolvedCbioType;

    @Autowired
    FinderTransformer(ExportDataToTemplate exportDataToTemplate, CbpTransformer cbioTransformer){
       this.exportDataToTemplate = exportDataToTemplate;
       this.cbioTransformer = cbioTransformer;
    }

    void run(File dataDirectory, File overideTemplateDir, File overideExportDir, File ingestFile,  String provider, boolean loadAll, String cmdCbioType) throws IOException {
        resolveDirEnv(dataDirectory,overideTemplateDir,overideExportDir);
        if(loadAll){
            exportDataToTemplate.exportAllGroups(rootDir);
        }
        else if (provider != null && !provider.isEmpty()) {
            exportDataToTemplate.export(rootDir, provider);
        }
        else if (cmdCbioType != null && !cmdCbioType.isEmpty()){
           runCbioportal(ingestFile, cmdCbioType);
        }
    }

    private void resolveDirEnv(File dataDirectory, File overideTemplateDir, File overideExportDir) throws IOException{
        rootDir = new File(defaultDirectory);
        templateDir = new File(defaultTemplateDirectory);
        exportDir = new File(defaultExportDirectory);

        if(dataDirectory != null && dataDirectory.exists()){
            log.info(String.format("Using %s as root directory", dataDirectory));
            rootDir = dataDirectory;
        }
        if(doesFileExists(overideTemplateDir)){
            log.info(String.format("Using %s as template directory", overideTemplateDir));
            templateDir = overideTemplateDir;
        }
        if(doesFileExists(overideExportDir)){
            log.info(String.format("Using %s as export directory", overideExportDir));
            exportDir = overideExportDir;
        }
        if (!(doesFileExists(rootDir) && doesFileExists(templateDir) && doesFileExists(exportDir))){
            throw new IOException("Erorr resolving root, template, or export directory. Either default directories in the Finder root directory or arguments");
        }
    }

    private void runCbioportal(File ingestFile, String cmdCbioType) throws IOException {
        resolveCbioType(cmdCbioType);
        if (!(doesFileExists(ingestFile)))
        {
            throw new IOException("Ingest file directory does not exist");
        }
        cbioTransformer.exportCBP(exportDir,templateDir,ingestFile,resolvedCbioType);
    }

    private void resolveCbioType(String cmdCbioType){
        if (cmdCbioType.equals(cbioType.MUT.name())){
            resolvedCbioType = cbioType.MUT;
        }
        else if (cmdCbioType.equals(cbioType.GISTIC.name())){
            resolvedCbioType = cbioType.GISTIC;
        } else throw new IllegalArgumentException("Only MUT and GISTIC types are currently supported");
    }

    private boolean doesFileExists(File file) {
        return file != null && file.exists();
    }

    public void setDefaultDirectories(String defaultDirectory) {
        this.defaultDirectory = defaultDirectory;
         defaultTemplateDirectory = defaultDirectory + "/template";
         defaultExportDirectory = defaultDirectory + "/export";
    }
    public void setDefaultDirectory(String defaultDirectory){
        this.defaultDirectory = defaultDirectory;
    }

    public void setTemplateDir(File templateDir) { this.templateDir = templateDir; }

    public void setExportDir(File exportDir) { this.exportDir = exportDir; }
}
