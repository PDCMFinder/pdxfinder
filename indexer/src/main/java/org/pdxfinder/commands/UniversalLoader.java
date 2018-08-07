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

    private List<List<String>> patientSheetData;

    private FileInputStream excelFile;


    @PostConstruct
    public void init() {
        formatter = new HelpFormatter();
    }

    public UniversalLoader(DataImportService dataImportService) {
        this.dataImportService = dataImportService;
        this.patientSheetData = new ArrayList<>();
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

            //WorkbookFactory.create(excelFile);
            Workbook workbook = new XSSFWorkbook(excelFile);
            log.info("Loading template");
            loadTemplateData(workbook);
            workbook.close();
            excelFile.close();

            System.out.println(patientSheetData.toString());
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

        initializePatientSheetData(workbook.getSheetAt(1));
        createPatientTumors();
        createPatientTreatments();
        createPdxModels();
        createPdxModelValidations();
    }



    private void initializePatientSheetData(Sheet sheet){

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
            patientSheetData.add(dataRow);
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
