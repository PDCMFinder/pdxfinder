package org.pdxfinder.dataloaders.updog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.tablesaw.api.Table;

import java.util.Map;

public class Updog {

    private String provider;

    public Updog(String provider) {
        this.provider = provider;
    }

    private static final Logger log = LoggerFactory.getLogger(Updog.class);

    private Map<String, Table> pdxDataTables;
    private Map<String, Object> domainObjects;

    public void run() {
        readPdxDataForProvider();
        validatePdxDataTables();
        load();
    }

    private void readPdxDataForProvider() {
        TemplateReader templateReader = new TemplateReader(provider);
        pdxDataTables = templateReader.read();
    }

    public boolean validatePdxDataTables(){

        //instantiate a validator class
        TemplateValidator templateValidator = new TemplateValidator();
        return true;
    }


    public void load(){

        //create domain objects database nodes
        DomainObjectCreator doc = new DomainObjectCreator();
        //save db


    }


}
