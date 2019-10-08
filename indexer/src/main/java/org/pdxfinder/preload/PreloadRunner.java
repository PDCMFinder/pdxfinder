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
import java.util.Arrays;

@Component
public class PreloadRunner implements CommandLineRunner {

    public PreloadRunner() throws IOException {
    }

    @Value("${pdxfinder.root.dir}")
    private String finderRootDir;

    @Value("${pdxfinder.root.out}")
    private String finderOutput;

    Logger log = LoggerFactory.getLogger(PreloadRunner.class);

    ArrayList<File> omicFiles;
    OmicCrawler crawler = new OmicCrawler();
    PDX_XlsxReader reader = new PDX_XlsxReader();
    OmicHarmonizer harmonizer = new OmicHarmonizer(CHAINFILE);
    UtilityService utilityService = new UtilityService();

    private static final String CHAINFILE = "src/main/resources/LiftOverResources/hg19ToHg38.over.chain.gz";

    @Override
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("LIFT");
        OptionSet options = parser.parse(args);
        finderRootDir = UniversalLoader.stripTrailingSlash(finderRootDir);

        if (options.has("liftOver")) runLiftOver();
    }

    public void runLiftOver() throws IOException {


        omicFiles = crawler.run(new File(finderRootDir));

        omicFiles.forEach(f -> {

            OmicHarmonizer.OMIC dataType = determineOmicType(f);
            ArrayList<ArrayList<String>> sheet = getSheet(f);
            ArrayList<ArrayList<String>> outFile = null;

            try {


                outFile = harmonizer.runLiftOver(sheet, dataType);
                if (!(outFile.size() == 1 || outFile.isEmpty()))
                    makeOutFileDirAndSave(f, outFile);
                else
                    log.info("No data lifted for " + f.getPath());

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void makeOutFileDirAndSave(File f,ArrayList<ArrayList<String>> liftedSheet) throws IOException {

        String sourceNameRegex = "(?i).+UPDOG/(.+)/.+";
        String datatypeRegex = "(?i).+UPDOG/.+/(.+)";
        String sourceDir = f.getParent().replaceAll(sourceNameRegex, "$1");
        String dataType = f.getParent().replaceAll(datatypeRegex, "$1");

        Path outputRoot = Paths.get(URI.create("file://" + finderOutput));
        Path UPDOG = Paths.get(outputRoot.toString() + "/UPDOG");
        Path sourceFolder = Paths.get(UPDOG.toString() + "/" + sourceDir);
        Path sourceData = Paths.get(sourceFolder.toString() + "/" + dataType);
        Path outFile = Paths.get(sourceData.toString() + "/data.xlsx");

        if(Files.notExists(outputRoot))
            Files.createDirectory(outputRoot);
        if(Files.notExists(UPDOG))
            Files.createDirectory(UPDOG);
        if(Files.notExists(sourceFolder))
            Files.createDirectory(sourceFolder);
        if(Files.notExists(sourceData))
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
            System.out.println("lifting file " + f.getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sheet;
    }

    private void printSheets(ArrayList<ArrayList<String>> outFile){

        for(int i = 0; i < outFile.size(); i++){

            System.out.println(Arrays.toString(outFile.get(i).toArray()));
        }
    }

    public void setFinderRootDir(String finderRootDir) {
        this.finderRootDir = finderRootDir;
    }

    public void setFinderOutput(String finderOutput) {this.finderOutput = finderOutput;}
}
