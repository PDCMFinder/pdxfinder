package org.pdxfinder.dataloaders.updog;

import com.ibm.icu.impl.LocaleDisplayNamesImpl;
import org.hibernate.sql.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Updog {

    private String provider;

    public Updog(String provider) {
        this.provider = provider;
    }

    private static final Logger log = LoggerFactory.getLogger(Updog.class);

    private Map<String, PdxDataTable> pdxDataTables;
    private Map<String, Object> domainObjects;


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
        DomainObjectCreator doc = new DomainObjectCreator();
        //save db


    }


}
