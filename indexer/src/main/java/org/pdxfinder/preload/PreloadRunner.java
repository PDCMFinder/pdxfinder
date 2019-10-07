package org.pdxfinder.preload;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.pdxfinder.dataloaders.UniversalLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class PreloadRunner implements CommandLineRunner {

    public void setFinderRootDir(String finderRootDir) {
        this.finderRootDir = finderRootDir;
    }

    @Value("${pdxfinder.root.dir}")
    private String finderRootDir;

    @Value("${pdxfinder.root.out}")
    private String finderOutput;

    ArrayList<File> omicFiles;
    OmicCrawler crawler = new OmicCrawler();
    PDX_XlsxReader reader = new PDX_XlsxReader();
    OmicHarmonizer harmonizer = new OmicHarmonizer();


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

            ArrayList<ArrayList<String>> sheet = getSheet(f);
            harmonizer.setOmicSheet(sheet);
            ArrayList<ArrayList<String>> outFile = null;
            try {
                outFile = harmonizer.runLiftOver(CHAINFILE);
            } catch (IOException e) {
                e.printStackTrace();
            }
            printSheets(outFile);
        });
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
}
