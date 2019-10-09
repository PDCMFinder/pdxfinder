package org.pdxfinder.preload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class OmicCrawler {

    ArrayList<File> providersData;
    ArrayList<File> variantData = new ArrayList<>();

    Logger log = LoggerFactory.getLogger(OmicCrawler.class);

    public List<File> run(File UPDOG) throws IOException {
        return searchFileTreeForOmicData(UPDOG);
    }

    public List<File> searchFileTreeForOmicData(File rootDir) throws IOException {

        if(folderExists(rootDir)) {

            String updog = String.format("%s/data/UPDOG", rootDir);

            List<File> providerFolders = Arrays.asList(new File(updog).listFiles());

            providersData = returnMutAndCNASubFolders(providerFolders);

            variantData = getVariantdata(providersData);

        } else throw new IOException("Error root directory could not be found by the OmicCrawler");

        return variantData;
    }

    private ArrayList<File> returnMutAndCNASubFolders(List<File> rootDir){

        ArrayList<File> providers = new ArrayList<>();

        rootDir.forEach(f ->

            providers.addAll
                    (Arrays.stream(f.listFiles())
                            .filter(t -> t.getName().matches("(mut|cna)"))
                            .collect(Collectors.toCollection(ArrayList::new)))

        );
        return providers;
    }

    private ArrayList<File> getVariantdata(ArrayList<File> providersData) {

        if( ! providersData.isEmpty())
            return returnMutAndCNAFiles(providersData);
        else
            return new ArrayList<>();
    }

    private ArrayList<File> returnMutAndCNAFiles(List<File> providersData) {

        providersData.forEach(f ->

            variantData.addAll
                    (Arrays.stream(f.listFiles())
                            .filter(t -> t.getName().equals("data.xlsx"))
                            .collect(Collectors.toCollection(ArrayList::new)))
        );

        return variantData;
    }

    private boolean folderExists(File rootDir){
        return rootDir.exists();
    }
}
