package org.pdxfinder.preload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class OmicCrawler {

    ArrayList<File> providersData;
    ArrayList<File> variantData;

    Logger log = LoggerFactory.getLogger(OmicCrawler.class);

    public ArrayList<File> run(File UPDOG) throws IOException {
        return searchFileTreeForOmicData(UPDOG);
    }

    public ArrayList<File> searchFileTreeForOmicData(File rootDir) throws IOException {

        if(folderExists(rootDir)) {

            List<File> providerFolders = Arrays.asList(rootDir.listFiles());

            providersData = returnMutAndCNASubFolders(providerFolders);

            variantData = getVariantdata(providersData);

        } else

            throw new IOException("Error root directory could not be found by the OmicCrawler");
            log.error("Error root directory could not be found by the OmicCrawler");

        return variantData;
    }

    private ArrayList<File> returnMutAndCNASubFolders(List<File> rootDir){

        ArrayList<File> providers = new ArrayList<File>();

        rootDir.forEach(f -> {

            providers.addAll
                    (Arrays.stream(f.listFiles())
                            .filter(t -> t.getName().matches("(mut|cna)"))
                            .collect(Collectors.toCollection(ArrayList::new)));

        });
        return providers;
    }

    private ArrayList<File> getVariantdata(ArrayList<File> providersData) {

        if( ! providersData.isEmpty())
            return returnMutAndCNAFiles(providersData);
        else
            return new ArrayList<File>();
    }

    private ArrayList<File> returnMutAndCNAFiles(List<File> providersData) {

        ArrayList<File> variantData = new ArrayList<File>();

        providersData.forEach(f -> {

            variantData.addAll
                    (Arrays.stream(f.listFiles())
                            .filter(t -> t.getName().matches("data.xlsx"))
                            .collect(Collectors.toCollection(ArrayList::new)));

        });

        return variantData;
    }

    private boolean folderExists(File rootDir){
        return rootDir.exists();
    }
}
