package org.pdxfinder.preload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OmicDataCrawler {

    Logger log = LoggerFactory.getLogger(OmicDataCrawler.class);


    public List<File> searchFileTreeForOmicData(File rootDir) {


        List<File> providersData = new ArrayList<File>();

        if(rootFolderExists(rootDir)) {
            List<File> providerFolders = Arrays.asList(rootDir.listFiles());

            providerFolders.forEach(f -> {

                providersData.addAll
                        (Arrays.stream(f.listFiles())
                        .filter(t -> t.getName().equals("mut"))
                        .collect(Collectors.toCollection(ArrayList::new)));

            });

        } else

            log.error("Error root directory could not be found by the OmicCrawler");

        return providersData;
    }

    private boolean rootFolderExists(File rootDir){
        return rootDir.exists();
    }

}
