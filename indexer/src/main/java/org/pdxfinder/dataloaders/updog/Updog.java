package org.pdxfinder.dataloaders.updog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Updog {

    private String provider;

    public Updog(String provider) {
        this.provider = provider;
    }

    private static final Logger log = LoggerFactory.getLogger(Updog.class);

    private void readPdxDataTable() {
        PdxDataTable pdxDataTable = new PdxDataTable(provider);
        pdxDataTable.readData();
    }

}
