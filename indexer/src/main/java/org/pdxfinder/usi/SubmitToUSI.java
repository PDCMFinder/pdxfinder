package org.pdxfinder.usi;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.pdxfinder.services.DataImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/*
 * Created by csaba on 21/11/2018.
 */
@Component
@Order(value = 91)
public class SubmitToUSI implements CommandLineRunner{

    private final static Logger log = LoggerFactory.getLogger(SubmitToUSI.class);

    private DataImportService dataImportService;

    String jsonToSubmit;


    public SubmitToUSI(DataImportService dataImportService) {
        this.dataImportService = dataImportService;
    }

    @Override
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("submitToUSI", "Submitting to USI");
        OptionSet options = parser.parse(args);

        if (options.has("submitToUSI")) {

            submit();

        }
    }


    private void submit(){

        //get token


        //get data to submit
        jsonToSubmit = "{}";

        /*
        {
            "alias" : "PDMR 001",
                "title" : "PDMR 001",
                "description" : "Material derived from cell line NA12878",
                "attributes" : {
            "Project" : [ {
                "value" : "PDXFinder"
            } ],
            "DataType" : [ {
                "value" : "PDX"
            } ],
            "Sample ID" : [ {
                "value" : "PDXFinder"
            } ],
            "Sample Origin" : [ {
                "value" : "PDXFinder"
            } ],
            "Passage" : [ {
                "value" : "PDXFinder"
            } ],
            "Sample provider" : [ {
                "value" : "PDXFinder"
            } ],
            "Patient sex from which the tumor was extracted" : [ {
                "value" : "PDXFinder"
            } ],
            "Patient age at collection" : [ {
                "value" : "PDXFinder"
            } ],
            "Patient diagnosis at time of collection" : [ {
                "value" : "PDXFinder"
            } ],
            "Patient collected tumour type" : [ {
                "value" : "PDXFinder"
            } ],
            "Patient primary site" : [ {
                "value" : "PDXFinder"
            } ],
            "Patient site of collection" : [ {
                "value" : "PDX"
            } ],
            "PDX engraftment host strain name" : [ {
                "value" : "PDX"
            } ],
            "PDX engraftment host strain nomenclature" : [ {
                "value" : "PDX"
            } ],
            "PDX engraftment site" : [ {
                "value" : "PDX"
            } ],
            "Related PDX Model ID" : [ {
                "value" : "PDX"
            } ],
            "PDXFinder url" : [ {
                "value" : "PDX"
            } ]
        },
            "sampleRelationships" : [ ],
            "taxonId" : 9606,
                "taxon" : "Homo sapiens",
                "releaseDate" : "2017-01-01"
        }

        */


        //submit data

        //get response

        //store response


    }








}
