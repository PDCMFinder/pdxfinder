package org.pdxfinder.commands;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.apache.poi.hssf.usermodel.*;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.pdxfinder.services.DataImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/*
 * Created by csaba on 06/08/2018.
 */
@Component
@Order(value = 50)
public class UniversalLoader implements CommandLineRunner{


    private final static Logger log = LoggerFactory.getLogger(UniversalLoader.class);

    private DataImportService dataImportService;

    @Value("${universal.template.files}")
    private String[] templateFiles;

    private FileInputStream excelFile;
    private Workbook workbook;
    private Sheet patientSheet;
    private Sheet patientTumorSheet;
    private Sheet patientTreatmentSheet;
    private Sheet pdxModelSheet;
    private Sheet pdxModelValidationSheet;


    public UniversalLoader(DataImportService dataImportService) {
        this.dataImportService = dataImportService;
    }

    @Override
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("loadUniversal", "Running the universal loader");
        parser.accepts("loadALL", "Load all");

        OptionSet options = parser.parse(args);

        if (options.has("loadUniversal") || options.has("loadALL")) {

            //deal with loading multiple templates here
            for(int i=0; i<templateFiles.length; i++){

                initializeWorkbook(templateFiles[i]);
                loadTemplateData();

            }

        }

    }



    private void initializeWorkbook(String fileName){

        try {

            excelFile = new FileInputStream(new File(fileName));
            workbook = new XSSFWorkbook(excelFile);

            initializeSheets();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void initializeSheets(){

        patientSheet = workbook.getSheetAt(0);
        patientTumorSheet = workbook.getSheetAt(1);
        patientTreatmentSheet = workbook.getSheetAt(2);
        pdxModelSheet = workbook.getSheetAt(3);
        pdxModelValidationSheet = workbook.getSheetAt(4);

    }

    private void loadTemplateData(){

        createPatients();
        createPatientTumors();
        createPatientTreatments();
        createPdxModels();
        createPdxModelValidations();
    }



    private void createPatients(){


    }

    private void createPatientTumors(){

    }

    private void createPatientTreatments(){

    }

    private void createPdxModels(){

    }

    private void createPdxModelValidations(){

    }





}
