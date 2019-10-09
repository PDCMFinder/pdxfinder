package org.pdxfinder.preload;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.pdxfinder.dataloaders.UniversalLoader;
import org.pdxfinder.services.UtilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class PreloadRunner implements CommandLineRunner {

    @Value("${pdxfinder.root.dir}")
    private String finderRootDir;

    @Value("${pdxfinder.root.out}")
    private String finderOutput;

    private Logger log = LoggerFactory.getLogger(PreloadRunner.class);

    private OmicCrawler crawler = new OmicCrawler();
    private PreloaderXlsxReader reader = new PreloaderXlsxReader();
    private OmicHarmonizer harmonizer = new OmicHarmonizer(CHAINFILE);
    private UtilityService utilityService = new UtilityService();

    private static final String CHAINFILE = "indexer/src/main/resources/LiftOverResources/hg19ToHg38.over.chain.gz";

    @Override
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("LIFT");
        OptionSet options = parser.parse(args);
        finderRootDir = UniversalLoader.stripTrailingSlash(finderRootDir);

        if (options.has("LIFT")) runLiftOver();
    }

    public void runLiftOver() throws IOException {


        List<File> omicFiles = crawler.run(new File(finderRootDir));

        omicFiles.forEach(f -> {

            OmicHarmonizer.OMIC dataType = determineOmicType(f);
            ArrayList<ArrayList<String>> sheet = getSheet(f);
            ArrayList<ArrayList<String>> outFile;

            try {
                outFile = harmonizer.runLiftOver(sheet, dataType);
                if (!(outFile.size() == 1 || outFile.isEmpty()))
                    makeOutFileDirAndSave(f, outFile);
                else
                    log.info(String.format("No data lifted for %s",f.getName()));
            } catch (IOException e) {
                log.error(e.toString());
            }
        });
    }

    private void makeOutFileDirAndSave(File f,ArrayList<ArrayList<String>> liftedSheet) throws IOException {

        String sourceNameRegex = "(?i).{1,150}UPDOG/(.{1,10})/.{1,3}";
        String datatypeRegex = "(?i).{1,50}UPDOG/.{1,10}/(.{1,3})";
        String sourceDir = f.getParent().replaceAll(sourceNameRegex, "$1");
        String dataType = f.getParent().replaceAll(datatypeRegex, "$1");

        Path outputRoot = Paths.get(URI.create("file://" + finderOutput));
        Path updog = Paths.get(outputRoot.toString() + "/UPDOG");
        Path sourceFolder = Paths.get(updog.toString() + "/" + sourceDir);
        Path sourceData = Paths.get(sourceFolder.toString() + "/" + dataType);
        Path outFile = Paths.get(sourceData.toString() + "/data.xlsx");

        if(!outputRoot.toFile().exists())
            Files.createDirectory(outputRoot);
        if(!updog.toFile().exists())
            Files.createDirectory(updog);
        if(!sourceFolder.toFile().exists())
            Files.createDirectory(sourceFolder);
        if(!sourceData.toFile().exists())
            Files.createDirectory(sourceData);

        utilityService.writeXLSXFile2(liftedSheet, outFile.toString(), "Omic");
    }

    private OmicHarmonizer.OMIC determineOmicType(File f){
       if(f.getParent().matches(".+/mut")) return OmicHarmonizer.OMIC.MUT;
       else if(f.getParent().matches(".+/cna")) return OmicHarmonizer.OMIC.CNA;
       else return null;
    }

    private ArrayList<ArrayList<String>> getSheet(File f){

        ArrayList<ArrayList<String>> sheet = null;
        try {
            sheet = reader.readFirstSheet(f);
            log.info(String.format("Lifting file %s", f.getCanonicalFile()));
        } catch (IOException e) {
            log.error(e.toString());
        }
        return sheet;
    }

    public void setFinderRootDir(String finderRootDir) {
        this.finderRootDir = finderRootDir;
    }

    public void setFinderOutput(String finderOutput) {this.finderOutput = finderOutput;}
}
