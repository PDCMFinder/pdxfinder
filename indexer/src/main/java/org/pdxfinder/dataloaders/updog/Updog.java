package org.pdxfinder.dataloaders.updog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

public class Updog {

    private String provider;

    public Updog(String provider) {
        this.provider = provider;
    }

    private static final Logger log = LoggerFactory.getLogger(Updog.class);

    private Map<String, PdxDataTable> pdxDataTables;
    private Map<String, Set<Object>> domainObjects;


    private void readPdxDataTable() {


        PdxDataTable pdxDataTables = new PdxDataTable(provider);
        pdxDataTables.readData();
    }

    public boolean validateTemplate(){

        //instantiate a validator class
        TemplateValidator templateValidator = new TemplateValidator();
        return true;
    }


    public void load(){

        //create domain objects database nodes
        DomainObjectCreator doc = new DomainObjectCreator(pdxDataTables);
        //save db


    }


}
