package org.pdxfinder.commandline;

import org.pdxfinder.utils.CbpTransformer;
import org.pdxfinder.utils.CbpTransformer.cbioType;
;
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
    private String defaultTemplateDirectory;
    private String defaultExportDirectory;
    private CbpTransformer cbioTransformer;
    private File rootDir;
    private File templateDir;
    private File exportDir;
    private cbioType resolvedCbioType;

    @Autowired
    FinderTransformer(CbpTransformer cbioTransformer){
       this.cbioTransformer = cbioTransformer;
    }

    void run(File dataDirectory, File overideTemplateDir, File overideExportDir, File ingestFile, String cmdCbioType) throws IOException {
        resolveRootDir(dataDirectory);
        resolveTemplateDir(overideTemplateDir);
        resolveExportDir(overideExportDir);

        if (cmdCbioType != null && !cmdCbioType.isEmpty()){
           runCbioportal(ingestFile, cmdCbioType);
        }
    }

    private void resolveRootDir(File dataDirectory) throws IOException {
        if (dataDirectory != null && dataDirectory.exists()) {
            log.info(String.format("Using %s as root directory", dataDirectory));
            rootDir = dataDirectory;
        } else {
            rootDir = new File(defaultDirectory);
        }
        if (!(fileExists(rootDir))) {
            throw new IOException(String.format("Erorr resolving root data directory: %s", rootDir.getAbsoluteFile()));
        }
    }

    private void resolveTemplateDir(File overideTemplateDir) throws IOException {
        if (fileExists(overideTemplateDir)) {
            log.info(String.format("Using %s as template directory", overideTemplateDir));
            templateDir = overideTemplateDir;
        } else {
            templateDir = new File(rootDir.getAbsoluteFile() + "/template");
        }
        if (!(fileExists(templateDir))){
            throw new IOException(String.format("Erorr resolving template directory %s", templateDir.getAbsoluteFile()));
        }
    }

    private void resolveExportDir(File overideExportDir) throws IOException {

        if(fileExists(overideExportDir)){
            log.info(String.format("Using %s as export directory", overideExportDir));
            exportDir = overideExportDir;
        } else {
            exportDir = new File(rootDir.getAbsoluteFile() + "/export");
        }
        if (!(fileExists(exportDir))){
            throw new IOException(String.format("Erorr resolving export directory %s", exportDir.getAbsoluteFile()));
        }
    }

    private void runCbioportal(File ingestFile, String cmdCbioType) throws IOException {
        resolveCbioType(cmdCbioType);
        if (!(fileExists(ingestFile)))
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

    private boolean fileExists(File file) {
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
