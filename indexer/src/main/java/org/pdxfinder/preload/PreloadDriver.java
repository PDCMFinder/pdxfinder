package org.pdxfinder.preload;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class PreloadDriver {

    ArrayList<File> omicFiles;

    public void run() throws IOException {

        File outFile;

        OmicCrawler crawler = new OmicCrawler();
        PDX_XlsxReader reader = new PDX_XlsxReader();
        omicFiles = crawler.run(new File("/home/afollette/Documents/data/UPDOG"));

        omicFiles.forEach(f -> {

            //outFile = OmicHarmonizer(f);
            //waterMarkFile(outFile);
            //saveFile(outFile);
        });
    }
}
