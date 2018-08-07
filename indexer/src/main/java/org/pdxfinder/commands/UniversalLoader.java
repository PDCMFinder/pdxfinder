package org.pdxfinder.commands;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;


import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.neo4j.ogm.session.Session;

import org.pdxfinder.services.DataImportService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.*;

/**
 * Load data from JAX.
 */
@Component
@Order(value = 0)
public class UniversalLoader implements CommandLineRunner {

    private final static Logger log = LoggerFactory.getLogger(UniversalLoader.class);

    private Options options;
    private CommandLineParser parser;
    private CommandLine cmd;
    private HelpFormatter formatter;

    private DataImportService dataImportService;
    private Session session;

    @Value("${universal.template.files}")
    private String templateFiles;


    private FileInputStream excelFile;


    @PostConstruct
    public void init() {
        formatter = new HelpFormatter();
    }

    public UniversalLoader(DataImportService dataImportService) {
        this.dataImportService = dataImportService;
    }

    @Override
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("loadUniversal", "Run universal loader");
        parser.accepts("loadALL", "Load all, including JAX PDX data");
        OptionSet options = parser.parse(args);

        if (options.has("loadUniversal") || options.has("loadALL")  || options.has("loadSlim")) {

            log.info("Running universal");
            FileInputStream excelFile = new FileInputStream(new File(templateFiles));

            WorkbookFactory.create(excelFile);
            Workbook workbook = new XSSFWorkbook(excelFile);

            Sheet datatypeSheet = workbook.getSheetAt(0);

        }
    }





    private void initializeWorkbook(String fileName){

        try {

            excelFile = new FileInputStream(new File(fileName));
            Workbook workbook = new XSSFWorkbook(excelFile);

            loadTemplateData(workbook);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void initializeSheets(Workbook workbook){

        Sheet patientSheet = workbook.getSheetAt(0);
        Sheet patientTumorSheet = workbook.getSheetAt(1);
        Sheet patientTreatmentSheet = workbook.getSheetAt(2);
        Sheet pdxModelSheet = workbook.getSheetAt(3);
        Sheet pdxModelValidationSheet = workbook.getSheetAt(4);

    }

    private void loadTemplateData(Workbook workbook){

        createPatients(workbook.getSheetAt(0));
        createPatientTumors();
        createPatientTreatments();
        createPdxModels();
        createPdxModelValidations();
    }



    private void createPatients(Sheet patientSheet){

        Iterator<Row> iterator = patientSheet.iterator();
        int rowCounter = 0;
        while (iterator.hasNext()) {

            Row currentRow = iterator.next();
            rowCounter++;

            if(rowCounter<5) continue;

            Iterator<Cell> cellIterator = currentRow.iterator();

            while (cellIterator.hasNext()) {

                Cell currentCell = cellIterator.next();
                //getCellTypeEnum shown as deprecated for version 3.15
                //getCellTypeEnum ill be renamed to getCellType starting from version 4.0

                System.out.print(currentCell.getStringCellValue() + "--");


            }
        }
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
