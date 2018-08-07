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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.*;

/*
 * Created by csaba on 06/08/2018.
 */
@Component
@Order(value = 0)
/**
 *
 * aka UPDOG: Universal PdxData tO Graph
 */
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

    private List<List<String>> patientSheetData;
    private List<List<String>> patientTumorSheetData;
    private List<List<String>> patientTreatmentSheetData;
    private List<List<String>> pdxModelSheetData;
    private List<List<String>> pdxModelVariationSheetData;

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

        if (options.has("loadUniversal")) {

            log.info("Running universal");
            FileInputStream excelFile = new FileInputStream(new File(templateFiles));

            Workbook workbook = new XSSFWorkbook(excelFile);
            log.info("Loading template");
            initializeTemplateData(workbook);

            loadTemplateData();

            workbook.close();
            excelFile.close();

        }
    }


    /**
     * Loads the data from the spreadsheet and stores it in lists
     * @param workbook
     */
    private void initializeTemplateData(Workbook workbook){

        patientSheetData = new ArrayList<>();
        patientTumorSheetData = new ArrayList<>();
        patientTreatmentSheetData = new ArrayList<>();
        pdxModelSheetData = new ArrayList<>();
        pdxModelVariationSheetData = new ArrayList<>();

        initializeSheetData(workbook.getSheetAt(1), "patientSheetData");
        initializeSheetData(workbook.getSheetAt(2), "patientTumorSheetData");
        initializeSheetData(workbook.getSheetAt(3), "patientTreatmentSheetData");
        initializeSheetData(workbook.getSheetAt(4), "pdxModelSheetData");
        initializeSheetData(workbook.getSheetAt(5), "pdxModelVariationSheetData");
    }

    private void initializeSheetData(Sheet sheet, String sheetName){

        Iterator<Row> iterator = sheet.iterator();
        int rowCounter = 0;
        while (iterator.hasNext()) {

            Row currentRow = iterator.next();
            rowCounter++;

            if(rowCounter<6) continue;

            Iterator<Cell> cellIterator = currentRow.iterator();
            List dataRow = new ArrayList();
            boolean isFirstColumn = true;
            while (cellIterator.hasNext()) {

                Cell currentCell = cellIterator.next();
                //skip the first column
                if(isFirstColumn){
                    isFirstColumn = false;
                    continue;
                }

                //getCellTypeEnum shown as deprecated for version 3.15
                //getCellTypeEnum will be renamed to getCellType starting from version 4.0

                String value = null;
                switch (currentCell.getCellType()) {
                    case Cell.CELL_TYPE_STRING:
                        value = currentCell.getStringCellValue();
                        break;
                    case Cell.CELL_TYPE_BOOLEAN:
                        value = String.valueOf(currentCell.getBooleanCellValue());
                        break;
                    case Cell.CELL_TYPE_NUMERIC:
                        value = String.valueOf(currentCell.getNumericCellValue());
                        break;
                }

                dataRow.add(value);
            }


            if(sheetName.equals("patientSheetData")){

                patientSheetData.add(dataRow);
            }
            else if(sheetName.equals("patientTumorSheetData")){

                patientTumorSheetData.add(dataRow);
            }
            else if(sheetName.equals("patientTreatmentSheetData")){

                patientTreatmentSheetData.add(dataRow);
            }
            else if(sheetName.equals("pdxModelSheetData")){

                pdxModelSheetData.add(dataRow);
            }
            else if(sheetName.equals("pdxModelVariationSheetData")){

                pdxModelVariationSheetData.add(dataRow);
            }

        }
    }

    /**
     * Loads the data from the lists into the DB
     */
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
