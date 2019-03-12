package org.pdxfinder.commands.dataloaders;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;


import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.neo4j.ogm.session.Session;

import org.pdxfinder.graph.dao.*;
import org.pdxfinder.reportmanager.ReportManager;
import org.pdxfinder.services.DataImportService;

import org.pdxfinder.services.ds.Standardizer;
import org.pdxfinder.services.dto.NodeSuggestionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

import org.apache.poi.ss.usermodel.*;

/*
 * Created by csaba on 06/08/2018.
 */

@Component
@Order(value = 0)
/**
 *
 * aka UPDOG: Universal PdxData tO Graph
 *
 * We should call it UPDOG. And it sets up a good joke. Any newcomer says "What's UPDOG?" we can say "Nothing much, what's up with you?"
 */
public class UniversalLoader implements CommandLineRunner, ApplicationContextAware {

    private final static Logger log = LoggerFactory.getLogger(UniversalLoader.class);

    Logger logger = LoggerFactory.getLogger(UniversalLoader.class);

    private Options options;
    private CommandLineParser parser;
    private CommandLine cmd;
    private HelpFormatter formatter;

    private DataImportService dataImportService;
    private Session session;

    static ApplicationContext context;
    ReportManager reportManager;

    @Value("${pdxfinder.data.root.dir}")
    private String dataRootDir;

    /**
     * Placeholder for the data stored in the "patient" tab
     */
    private List<List<String>> patientSheetData;


    /**
     * Placeholder for the data stored in the "patienttumor at collection" tab
     */
    private List<List<String>> patientTumorSheetData;

    /**
     * Placeholder for the data stored in the "patient treatment information" tab
     */
    private List<List<String>> patientTreatmentSheetData;

    /**
     * Placeholder for the data stored in the "PDX model detail" tab
     */
    private List<List<String>> pdxModelSheetData;


    /**
     * Placeholder for the data stored in the "PDX model validation" tab
     */
    private List<List<String>> pdxModelValidationSheetData;


    /**
     * Placeholder for the data stored in the "dataset derived from patient" tab
     */
    private List<List<String>> derivedDatasetSheetData;

    /**
     * Placeholder for the data stored in the "sharing and contact" tab
     */
    private List<List<String>> sharingAndContactSheetData;


    /**
     * Placeholder for the data stored in the "breast and or colorectal diagno" tab
     */
    private List<List<String>> breastAndOrColorectalDiagnoSheetData;


    /**
     * Placeholder for the data stored in the "Loader related data tab
     */
    private List<List<String>> loaderRelatedDataSheetData;

    private Group ds;

    private Boolean stopLoading;


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
        parser.accepts("loadUniversal", "Running universal loader (UPDOG)");
        parser.accepts("loadALL", "Load all, running universal loader (UPDOG)");
        OptionSet options = parser.parse(args);

        if (options.has("loadUniversal") || options.has("loadALL")) {

            reportManager = (ReportManager) context.getBean("ReportManager");


            File folder = new File(dataRootDir + "UPDOG/");

            if (folder.exists()) {

                File[] updogDirs = folder.listFiles();

                if (updogDirs.length == 0) {

                    log.warn("No subdirs found for the universal loader, skipping");
                } else {

                    for (int i = 0; i < updogDirs.length; i++) {

                        if (updogDirs[i].isDirectory()) {

                            String templateFileStr = dataRootDir + "UPDOG/" + updogDirs[i].getName() + "/template.xlsx";

                            File template = new File(templateFileStr);

                            //found the template, load it
                            if (template.exists()) {

                                log.info("******************************************************");
                                log.info("* Starting universal loader                          *");
                                log.info("******************************************************");


                                FileInputStream excelFile = new FileInputStream(new File(templateFileStr));

                                Workbook workbook = new XSSFWorkbook(excelFile);
                                log.info("Loading template from " + templateFileStr);

                                initializeTemplateData(workbook);

                                loadTemplateData();

                                workbook.close();
                                excelFile.close();

                                log.info("******************************************************");
                                log.info("* Finished running universal loader                  *");
                                log.info("******************************************************");

                            } else {

                                log.error("No template file found for universal loader in " + updogDirs[i]);
                            }

                        }
                    }
                }

            }
            //NO UNIVERSAL TEMPLATES, SKIP
            else {

                log.warn("No UPDOG directory found. Who let the dog out?");
            }


        }
    }


    /**
     * Loads the data from the spreadsheet and stores it in lists
     *
     * @param workbook
     */
    private void initializeTemplateData(Workbook workbook) {

        log.info("******************************************************");
        log.info("* Initializing Sheet data                            *");
        log.info("******************************************************");

        ds = null;
        stopLoading = false;

        patientSheetData = new ArrayList<>();
        patientTumorSheetData = new ArrayList<>();
        patientTreatmentSheetData = new ArrayList<>();
        pdxModelSheetData = new ArrayList<>();
        pdxModelValidationSheetData = new ArrayList<>();
        derivedDatasetSheetData = new ArrayList<>();
        breastAndOrColorectalDiagnoSheetData = new ArrayList<>();
        sharingAndContactSheetData = new ArrayList<>();
        loaderRelatedDataSheetData = new ArrayList<>();

        initializeSheetData(workbook.getSheetAt(1), patientSheetData);
        initializeSheetData(workbook.getSheetAt(2), patientTumorSheetData);
        initializeSheetData(workbook.getSheetAt(3), patientTreatmentSheetData);
        initializeSheetData(workbook.getSheetAt(4), pdxModelSheetData);
        initializeSheetData(workbook.getSheetAt(5), pdxModelValidationSheetData);
        initializeSheetData(workbook.getSheetAt(6), derivedDatasetSheetData);
        initializeSheetData(workbook.getSheetAt(7), sharingAndContactSheetData);
        initializeSheetData(workbook.getSheetAt(8), breastAndOrColorectalDiagnoSheetData);
        initializeSheetData(workbook.getSheetAt(9), loaderRelatedDataSheetData);
    }

    /**
     * Loads the data from a spreadsheet tab into a placeholder
     *
     * @param sheet
     * @param sheetData
     */
    private void initializeSheetData(Sheet sheet, List<List<String>> sheetData) {

        Iterator<Row> iterator = sheet.iterator();
        int rowCounter = 0;
        while (iterator.hasNext()) {

            Row currentRow = iterator.next();
            rowCounter++;

            if (rowCounter < 6) continue;

            Iterator<Cell> cellIterator = currentRow.iterator();
            List dataRow = new ArrayList();
            boolean isFirstColumn = true;
            while (cellIterator.hasNext()) {

                Cell currentCell = cellIterator.next();
                //skip the first column
                if (isFirstColumn) {
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
            //check if there is some data in the row and they are not all nulls
            if (dataRow.size() > 0 && !isRowOfNulls(dataRow)) {

               sheetData.add(dataRow);
                

            }

        }
    }

    /**
     * Loads the data from the lists into the DB
     */
    private void loadTemplateData() {

        //:: DON'T CHANGE THE ORDER OF THESE METHODS UNLESS YOU WANT TO RISK THE UNIVERSE TO COLLAPSE!
        createDataSourceGroup();
        createPatients();
        createPatientTumors();
        createPatientTreatments();



        createPdxModelDetails();
        createPdxModelValidations();

        createDerivedPatientModelDataset();

        createSharingAndContacts();

        createBreastAndOrColorectalData();

    }

    private void createDataSourceGroup() {

        //TODO: this data has to come from the spreadsheet, I am using constants for now

        log.info("******************************************************");
        log.info("* Creating DataSource                                *");
        log.info("******************************************************");

        if (loaderRelatedDataSheetData.size() != 1) {
            stopLoading = true;

            log.error("Zero or multiple provider definitions! Loading process is terminated.");
            return;
        }

        String providerName = loaderRelatedDataSheetData.get(0).get(0);
        String providerAbbreviation = loaderRelatedDataSheetData.get(0).get(1);
        String sourceUrl = loaderRelatedDataSheetData.get(0).get(2);

        if (providerName.isEmpty() || providerAbbreviation.isEmpty()) {
            stopLoading = true;

            log.error("Missing provider name or abbreviation! Loading process is terminated!");
            return;
        }

        ds = dataImportService.getProviderGroup(providerName, providerAbbreviation, "", "",  "", sourceUrl);

    }

    private void createPatients() {

        if (stopLoading) return;

        log.info("******************************************************");
        log.info("* Creating Patients                                  *");
        log.info("******************************************************");

        for (List<String> patientRow : patientSheetData) {

            String patientId = patientRow.get(0);
            String sex = patientRow.get(1);
            String cancerHistory = patientRow.get(2);
            String ethnicity = patientRow.get(3);
            String firstDiagnosis = patientRow.get(4);
            String ageAtFirstDiagnosis = patientRow.get(5);

            if (patientId != null && ds != null) {

                Patient patient = dataImportService.createPatient(patientId, ds, sex, "", Standardizer.getEthnicity(ethnicity));
                patient.setCancerRelevantHistory(cancerHistory);
                patient.setFirstDiagnosis(firstDiagnosis);
                patient.setAgeAtFirstDiagnosis(ageAtFirstDiagnosis);

                dataImportService.savePatient(patient);

            }
        }
    }

    private void createPatientTumors() {

        if (stopLoading) return;

        log.info("******************************************************");
        log.info("* Creating Patient samples and snapshots             *");
        log.info("******************************************************");

        int row = 6;
        log.info("Tumor row number: " + patientTumorSheetData.size());
        for (List<String> patientTumorRow : patientTumorSheetData) {

            try {
                String patientId = patientTumorRow.get(0);
                String sampleId = patientTumorRow.get(1);
                String modelId = patientTumorRow.get(19);

                //skip rows where patient, model or sample id is null
                if (patientId == null || sampleId == null || modelId == null) continue;

                String dateOfCollection = patientTumorRow.get(2);
                String collectionEvent = patientTumorRow.get(3);
                String elapsedTime = patientTumorRow.get(4);
                String ageAtCollection = patientTumorRow.get(5);
                String diagnosis = patientTumorRow.get(6);
                String tumorType = patientTumorRow.get(7);
                String originTissue = patientTumorRow.get(8);
                String collectionSite = patientTumorRow.get(9);
                String stage = patientTumorRow.get(10);
                String stageClassification = patientTumorRow.get(11);
                String grade = patientTumorRow.get(12);
                String gradeClassification = patientTumorRow.get(13);
                String virologyStatus = patientTumorRow.get(14);
                String treatmentNaive = patientTumorRow.get(16);


                if (modelId.isEmpty()) {
                    log.error("Missing corresponding Model ID in row " + row);
                    continue;
                }


                //hack to avoid 0.0 values and negative numbers
                elapsedTime = elapsedTime.replaceAll("[^0-9]", "");

                Patient patient = dataImportService.getPatientWithSnapshots(patientId, ds);

                if (patient == null) {

                    log.error("Patient does not exist, can not create tumor for " + patientId);
                    continue;
                }

                //need this trick to remove float values, ie: patient age = 30.0
                if (!ageAtCollection.equals("Not Specified")) {
                    int ageAtColl = (int) Float.parseFloat(ageAtCollection);
                    ageAtCollection = ageAtColl + "";
                }


                PatientSnapshot ps = dataImportService.getPatientSnapshot(patient, ageAtCollection, dateOfCollection, collectionEvent, elapsedTime);
                ps.setTreatmentNaive(treatmentNaive);
                ps.setVirologyStatus(virologyStatus);


                //have the correct snapshot, create a human sample and link it to the snapshot
                tumorType = Standardizer.getTumorType(tumorType);


                //String sourceSampleId, String dataSource,  String typeStr, String diagnosis, String originStr,
                //String sampleSiteStr, String extractionMethod, Boolean normalTissue, String stage, String stageClassification,
                // String grade, String gradeClassification
                Sample sample = dataImportService.getSample(sampleId, ds.getAbbreviation(), tumorType, diagnosis, originTissue,
                        collectionSite, "", false, stage, stageClassification, grade, gradeClassification);

                ps.addSample(sample);


                ModelCreation mc = new ModelCreation();

                mc.setSourcePdxId(modelId);
                mc.setDataSource(ds.getAbbreviation());
                mc.setSample(sample);
                mc.addRelatedSample(sample);

                patient.hasSnapshot(ps);

                dataImportService.savePatient(patient);
                dataImportService.savePatientSnapshot(ps);
                dataImportService.saveModelCreation(mc);
                row++;

            } catch (Exception e) {
                log.error("Exception in row: " + row);
                e.printStackTrace();

            }

        }

    }

    private void createPatientTreatments() {

        if (stopLoading) return;

        log.info("******************************************************");
        log.info("* Creating Patient treatments                        *");
        log.info("******************************************************");

        int row = 6;
        for (List<String> patientTreatmentRow : patientTreatmentSheetData) {

            String patientId = patientTreatmentRow.get(0);
            String treatment = patientTreatmentRow.get(1);
            String dose = patientTreatmentRow.get(2);
            String startingDate = patientTreatmentRow.get(3);
            String duration = patientTreatmentRow.get(4);
            String response = patientTreatmentRow.get(5);
            String responseClassification = patientTreatmentRow.get(6);

            if (patientId.isEmpty() || treatment.isEmpty()) {

                log.error("Empty patient id or treatment in row " + row);
                continue;
            }


            Patient patient = dataImportService.findPatient(patientId, ds);

            if (patient == null) {

                log.error("Patient not found: " + patientId);
                continue;
            }

            PatientSnapshot ps = dataImportService.findLastPatientSnapshot(patientId, ds);

            //at this point a patient should have at least one snapshot, so if ps is null, thats an error
            if (ps == null) {

                log.error("No snapshot for patient: " + patientId);
                continue;
            }

            String treatmentUrl = loaderRelatedDataSheetData.get(0).get(3);

            TreatmentSummary ts = dataImportService.findTreatmentSummaryByPatientSnapshot(ps);

            if (ts == null) {
                ts = new TreatmentSummary();
                ts.setUrl(treatmentUrl);
            }

            TreatmentProtocol tp = dataImportService.getTreatmentProtocol(treatment, dose, response, responseClassification);

            if (tp != null) {

                //update treatment component type and duration
                for (TreatmentComponent tc : tp.getComponents()) {

                    tc.setDuration(duration);
                    //never control on humans!
                    tc.setType("Drug");
                }

                tp.setTreatmentDate(startingDate);
                ts.addTreatmentProtocol(tp);
            }


            ps.setTreatmentSummary(ts);
            dataImportService.savePatientSnapshot(ps);
            row++;
        }

    }

    private void createPdxModelDetails() {

        if (stopLoading) return;

        log.info("******************************************************");
        log.info("* Creating Model details                             *");
        log.info("******************************************************");

        int row = 6;

        for (List<String> modelDetailsRow : pdxModelSheetData) {

            String modelId = modelDetailsRow.get(0);
            String hostStrainName = modelDetailsRow.get(1);
            String hostStrainNomenclature = modelDetailsRow.get(2);
            String engraftmentSite = modelDetailsRow.get(3);
            String engraftmentType = modelDetailsRow.get(4);
            String engraftmentMaterial = modelDetailsRow.get(5);
            String engraftmentMaterialStatus = modelDetailsRow.get(6);
            String passage = modelDetailsRow.get(7);
            String pubmedIdString = modelDetailsRow.get(8);


            //check if essential values are not empty
            if (modelId.isEmpty() || hostStrainName.isEmpty() || hostStrainNomenclature.isEmpty() ||
                    engraftmentSite.isEmpty() || engraftmentType.isEmpty() || engraftmentMaterial.isEmpty()) {

                log.error("Missing essential value in row: " + row);
                row++;
                continue;

            }

            //at this point the corresponding pdx model node should be created and linked to a human sample
            ModelCreation model = null;
            try {
                model = dataImportService.findModelByIdAndDataSource(modelId, ds.getAbbreviation());

            }
            catch(Exception e){
                log.error("Error with model: "+ modelId);
                e.printStackTrace();
            }

            if (model == null) {

                log.error("Missing model, cannot add details: " + modelId);
                row++;
                continue;
            }

            //CREATING SPECIMENS: engraftment site, type and material

            EngraftmentSite es = dataImportService.getImplantationSite(engraftmentSite);
            EngraftmentType et = dataImportService.getImplantationType(engraftmentType);
            EngraftmentMaterial em = dataImportService.createEngraftmentMaterial(engraftmentMaterial, engraftmentMaterialStatus);

            HostStrain hostStrain = null;
            try{
                hostStrain = dataImportService.getHostStrain(hostStrainName, hostStrainNomenclature, "", "");
            }
            catch(Exception e){
                e.printStackTrace();
            }

            // passage = 1,3,5
            if (passage.contains(",")) {

                String[] passageArr = passage.split(",");

                for (int i = 0; i < passageArr.length; i++) {

                    //create specimens with engraftment data
                    Specimen specimen = new Specimen();
                    specimen.setPassage(passageArr[i]);
                    specimen.setEngraftmentSite(es);
                    specimen.setEngraftmentType(et);
                    specimen.setEngraftmentMaterial(em);
                    specimen.setHostStrain(hostStrain);

                    model.addSpecimen(specimen);
                }
            }
            //the passage is a single number
            else if (passage.matches("\\d+")) {

                //need this trick to get rid of fractures if there is any
                int passageInt = Integer.parseInt(passage);
                passage = String.valueOf(passageInt);

                //create specimens with engraftment data
                Specimen specimen = new Specimen();
                specimen.setPassage(passage);
                specimen.setEngraftmentSite(es);
                specimen.setEngraftmentType(et);
                specimen.setEngraftmentMaterial(em);
                specimen.setHostStrain(hostStrain);

                model.addSpecimen(specimen);

            }
            else {

                log.error("Not supported value(" + passage + ") for passage at row " + row);
            }

            //CREATE PUBLICATION GROUPS

            //check if pubmed id is in the right format, ie id starts with PMID
            if (!pubmedIdString.isEmpty() && pubmedIdString.toLowerCase().contains("pmid")) {

                // pubmed ids separated with a comma, create multiple groups
                if (pubmedIdString.contains(",")) {

                    String[] pubmedArr = pubmedIdString.split(",");

                    for (int i = 0; i < pubmedArr.length; i++) {

                        Group g = dataImportService.getPublicationGroup(pubmedArr[i].trim());
                        model.addGroup(g);
                    }
                }
                //single publication, create one group only
                else {

                    Group g = dataImportService.getPublicationGroup(pubmedIdString.trim());
                    model.addGroup(g);
                }

            }

            dataImportService.saveModelCreation(model);
            row++;
        }
    }

    private void createPdxModelValidations() {

        if (stopLoading) return;

        log.info("******************************************************");
        log.info("* Creating Model validations                         *");
        log.info("******************************************************");

        int row = 6;

        for (List<String> pdxModelValidationRow : pdxModelValidationSheetData) {


            String modelId = pdxModelValidationRow.get(0);
            String validationTechnique = pdxModelValidationRow.get(1);
            String validationDescription = pdxModelValidationRow.get(2);
            String passages = pdxModelValidationRow.get(3);
            String validationHostStrain = pdxModelValidationRow.get(4);

            if (modelId.isEmpty() || validationTechnique.isEmpty()) {

                log.error("Empty essential value in row: " + row);
                row++;
                continue;
            }


            //at this point the corresponding pdx model node should be created and available for lookup
            ModelCreation model = dataImportService.findModelByIdAndDataSource(modelId, ds.getAbbreviation());

            if (model == null) {

                log.error("Missing model, cannot add validation: " + modelId);
                row++;
                continue;
            }

            //need this trick to get rid of 0.0 if there is any
            String[] passageArr = passages.split(",");
            passages = "";

            for (int i = 0; i < passageArr.length; i++) {

                int passageInt = (int) Float.parseFloat(passageArr[i]);
                passages += String.valueOf(passageInt) + ",";
            }
            //remove that last comma
            passages = passages.substring(0, passages.length() - 1);

            QualityAssurance qa = new QualityAssurance();
            qa.setTechnology(validationTechnique);
            qa.setDescription(validationDescription);
            qa.setPassages(passages);

            model.addQualityAssurance(qa);
            dataImportService.saveModelCreation(model);

        }
    }

    private void createDerivedPatientModelDataset() {

        if (stopLoading) return;

        log.info("******************************************************");
        log.info("* Creating dataset derived from patients and models  *");
        log.info("******************************************************");

        int row = 6;

        for (List<String> derivedDatasetRow : derivedDatasetSheetData) {

            String sampleId = derivedDatasetRow.get(0);
            String origin = derivedDatasetRow.get(1);
            String passage = derivedDatasetRow.get(2);
            String nomenclature = derivedDatasetRow.get(3);
            String modelId = derivedDatasetRow.get(4);
            String molCharType = derivedDatasetRow.get(5);
            String platformName = derivedDatasetRow.get(6);
            String platformTechnology = derivedDatasetRow.get(7);
            String platformDescription = derivedDatasetRow.get(8);
            String analysisProtocol = derivedDatasetRow.get(9);
            //TODO: get additional fields from the sheet

            //check essential values

            if (sampleId.isEmpty() || origin.isEmpty() || modelId.isEmpty()
                    || molCharType.isEmpty() || platformName.isEmpty() || platformTechnology.isEmpty() || platformDescription.isEmpty()
                    || analysisProtocol.isEmpty()) {

                log.error("Missing essential value in row " + row);
                continue;
            }


            ModelCreation model;
            Sample sample;
            Platform platform;

            //patient sample
            if (origin.toLowerCase().equals("patient")) {

                sample = dataImportService.findHumanSample(modelId, ds.getAbbreviation());

                if (sample != null) {

                    platform = dataImportService.getPlatform(platformName, ds);
                    MolecularCharacterization mc = new MolecularCharacterization();
                    mc.setPlatform(platform);
                    mc.setType(molCharType);
                    mc.setTechnology(platformTechnology);
                    sample.addMolecularCharacterization(mc);
                    dataImportService.saveSample(sample);

                } else {

                    log.error("Unknown human sample with id: " + sampleId);
                    continue;
                }


            }


            //xenograft sample
            //specimen should have been created before
            else if (origin.toLowerCase().equals("xenograft")) {

                if (passage == null || passage.isEmpty()) {

                    log.error("Missing essential value Xenograft Passage in row " + row);
                    continue;
                }

                if (nomenclature == null || nomenclature.isEmpty()) {

                    log.error("Missing essential value nomenclature in row " + row);
                    continue;
                }

                //need this trick to get rid of 0.0 if there is any
                //if(passage.equals("0.0")) passage = "0";
                int passageInt = (int) Float.parseFloat(passage);
                passage = String.valueOf(passageInt);

                model = dataImportService.findModelByIdAndDataSourceWithSpecimensAndHostStrain(modelId, ds.getAbbreviation());

                //this specimen should have the appropriate hoststrain, too!
                Specimen specimen = dataImportService.findSpecimenByModelAndPassageAndNomenclature(model, passage, nomenclature);

                if(specimen != null){

                    sample = specimen.getSample();

                    if(sample == null) {

                        sample = new Sample();

                    }

                    platform = dataImportService.getPlatform(platformName, ds);
                    MolecularCharacterization mc = new MolecularCharacterization();
                    mc.setPlatform(platform);
                    mc.setType(molCharType);
                    mc.setTechnology(platformTechnology);
                    sample.addMolecularCharacterization(mc);
                    model.addRelatedSample(sample);

                    specimen.setSample(sample);

                    model.addSpecimen(specimen);
                    model.addRelatedSample(sample);
                    dataImportService.saveModelCreation(model);
                    dataImportService.saveSample(sample);

                }
                else{
                    // either specimen with passage or the host strain nomenclature was not created
                    log.error("Cannot find specimen with the following details: "+modelId+" "+passage+" "+nomenclature+ " in row: "+row);
                }


            }
            else{
                //origin is not patient nor xenograft
                log.error("Unknown sample origin in row "+row);
            }

            row++;
        }



    }


    private void createSharingAndContacts() {

        if (stopLoading) return;

        log.info("******************************************************");
        log.info("* Creating Sharing and contact info                  *");
        log.info("******************************************************");

        int row = 6;

        for (List<String> sharingAndContactRow : sharingAndContactSheetData) {

            String modelId = sharingAndContactRow.get(0);
            String dataProviderType = sharingAndContactRow.get(1);
            String modelAccessibility = sharingAndContactRow.get(2);
            String accessModalities = sharingAndContactRow.get(3);
            String contactEmail = sharingAndContactRow.get(4);
            String contactFormLink = sharingAndContactRow.get(6);
            String modelLinkToDB = sharingAndContactRow.get(7);
            String providerAbbreviation = sharingAndContactRow.get(9);
            String projectName = sharingAndContactRow.get(10);

            if (modelId.isEmpty()) {

                log.error("Model id is empty in row: " + row);
                row++;
                continue;
            }

            //at this point the corresponding pdx model node should be created

            ModelCreation model = dataImportService.findModelByIdAndDataSource(modelId, ds.getAbbreviation());

            if (model == null) {

                log.error("Missing model, cannot add sharing and contact info: " + modelId);
                row++;
                continue;
            }

            //Add contact provider and view data
            List<ExternalUrl> externalUrls = new ArrayList<>();
            externalUrls.add(dataImportService.getExternalUrl(ExternalUrl.Type.CONTACT, contactEmail));
            externalUrls.add(dataImportService.getExternalUrl(ExternalUrl.Type.SOURCE, modelLinkToDB));
            model.setExternalUrls(externalUrls);


            if (!projectName.isEmpty()) {

                Group project = dataImportService.getProjectGroup(projectName);
                model.addGroup(project);
            }

            dataImportService.saveModelCreation(model);

            //Update datasource
            ds.setProviderType(dataProviderType);
            ds.setAccessibility(modelAccessibility);
            ds.setAccessModalities(accessModalities);
            ds.setContact(contactEmail);


        }

        dataImportService.saveGroup(ds);

    }


    private void createBreastAndOrColorectalData(){

        if (stopLoading) return;

        log.info("******************************************************");
        log.info("* Creating breast and or colorectal markers          *");
        log.info("******************************************************");

        int row = 6;

        Map<String, MolecularCharacterization> patientMolChars = new HashMap<>();
        Map<String, MolecularCharacterization> xenoMolChars = new HashMap<>();

        //TODO: At some point deal with micro-satelite instability. Currently those rows are skipped. We don't want instability in our lives just yet.

        //first get all markers for the individual molchar objects
        for (List<String> dataRow : breastAndOrColorectalDiagnoSheetData) {

            String sampleId = dataRow.get(0);
            String origin = dataRow.get(1);
            String passage = dataRow.get(2);
            String nomenclature = dataRow.get(3);
            String modelId = dataRow.get(4);
            String markerSymbol = dataRow.get(5);
            String markerStatus = dataRow.get(6);
            String technique = dataRow.get(8);
            String platform = dataRow.get(9);

            if(origin.isEmpty() || modelId.isEmpty() || markerSymbol.isEmpty() || markerStatus.isEmpty() || technique.isEmpty() || platform.isEmpty()){
                log.error("Missing essential value in row " + row);
                continue;
            }

            MolecularCharacterization mc;
            Platform pl;
            Marker marker = null;
            NodeSuggestionDTO nsdto = dataImportService.getSuggestedMarker(this.getClass().getSimpleName(), ds.getAbbreviation(), modelId, markerSymbol, "IHC", "ImmunoHistoChemistry");

            if(nsdto.getNode() == null){

                //uh oh, we found an unrecognised marker symbol, abort, abort!!!!
                reportManager.addMessage(nsdto.getLogEntity());
                continue;
            }
            else{

                marker = (Marker) nsdto.getNode();

                if(origin.toLowerCase().equals("patient")){

                    //for patient related molchars it is sufficient to use the model + platform as the key
                    String mapKey = modelId + "___" + technique;

                    if(patientMolChars.containsKey(mapKey)) {
                        //get a previously created mc object = platform and type are already set
                        mc = patientMolChars.get(mapKey);
                    }

                    else {
                        //new mc object, need to set the platform, too
                        pl = dataImportService.getPlatform(technique, ds);

                        mc = new MolecularCharacterization();
                        mc.setPlatform(pl);
                        mc.setType(getMolcharType(technique));
                    }


                    MarkerAssociation ma = new MarkerAssociation();
                    ma.setMarker(marker);

                    if(technique.toLowerCase().equals("immunohistochemistry") || technique.toLowerCase().equals("fish")){

                        ma.setImmunoHistoChemistryResult(markerStatus);
                    }
                    //what if it is not ihc?

                    mc.addMarkerAssociation(ma);

                    //put molchar in the map if it was just created, but don't store molchars without type
                    if(!patientMolChars.containsKey(mapKey) && mc.getType()!= null){
                        patientMolChars.put(mapKey, mc);
                    }


                }
                else if(origin.toLowerCase().equals("xenograft")){

                    //need this trick to get rid of 0.0 if there is any
                    int passageInt = (int) Float.parseFloat(passage);
                    passage = String.valueOf(passageInt);

                    //for xenograft molchars use the combination of the modelid, nomenclature, passage and technique as the key
                    String mapKey = modelId + "___" + nomenclature + "___" + passage + "___" + technique;

                    if(xenoMolChars.containsKey(mapKey)) {
                        //get a previously created mc object = platform and type are already set
                        mc = xenoMolChars.get(mapKey);
                    }
                    else {
                        //new mc object, need to set the platform, too
                        pl = dataImportService.getPlatform(technique, ds);

                        mc = new MolecularCharacterization();
                        mc.setPlatform(pl);
                        mc.setType(getMolcharType(technique));

                    }


                    MarkerAssociation ma = new MarkerAssociation();
                    ma.setMarker(marker);

                    if(technique.toLowerCase().equals("immunohistochemistry") || technique.toLowerCase().equals("fish")){

                        ma.setImmunoHistoChemistryResult(markerStatus);
                    }
                    //what if it is not ihc? Would our world collapse entirely?

                    mc.addMarkerAssociation(ma);
                    //but don't store molchars without type
                    if(!xenoMolChars.containsKey(mapKey) && mc.getType()!= null){
                        xenoMolChars.put(mapKey, mc);
                    }

                }

            }








            row++;
        }


        //get the corresponding samples for the molchar objects, link them and save them.
        //patient samples
        for(Map.Entry<String, MolecularCharacterization> entry:patientMolChars.entrySet()){
            //key = model ID + "___" + platform
            String[] keyArr = entry.getKey().split("___");
            String key = keyArr[0];

            MolecularCharacterization mc = entry.getValue();

            Sample patientSample = dataImportService.findHumanSample(key, ds.getAbbreviation());

            if(patientSample != null){

                patientSample.addMolecularCharacterization(mc);
                dataImportService.saveSample(patientSample);
            }
            else{

                log.error("Failed to create molchar for patient sample! Model:"+key);
            }

        }

        //xeno samples
        for(Map.Entry<String, MolecularCharacterization> entry:xenoMolChars.entrySet()){
            //key =  modelId + "___" + nomenclature + "___" + passage + "___" + platform

            MolecularCharacterization mc = entry.getValue();
            String[] keyArr = entry.getKey().split("___");

            String modelId = keyArr[0];
            String nomenclature = keyArr[1];
            String passage = keyArr[2];

            ModelCreation model = dataImportService.findModelByIdAndDataSource(modelId, ds.getAbbreviation());
            Specimen specimen = dataImportService.findSpecimenByModelAndPassageAndNomenclature(model, passage, nomenclature);

            if(specimen != null){

                Sample xenoSample = specimen.getSample();

                if(xenoSample == null){
                    xenoSample = new Sample();

                }

                xenoSample.addMolecularCharacterization(mc);
                specimen.setSample(xenoSample);
                model.addRelatedSample(xenoSample);
                dataImportService.saveSpecimen(specimen);
                dataImportService.saveModelCreation(model);

            }
            else{
                log.error("Specimen not found. Model:"+modelId +" Passage: "+passage +" Nomenclature: "+nomenclature);
            }




        }

        log.info("******************************************************");
        log.info("* Finished creating breast and or colorectal markers *");
        log.info("******************************************************");

    }

    /**
     * Checks if a list consists of nulls only
     *
     * @param list
     */
    boolean isRowOfNulls(List list) {
        for (Object o : list)
            if (!(o == null))
                return false;
        return true;
    }


    private String getMolcharType(String technique){

        if(technique.toLowerCase().equals("immunohistochemistry")){
            return "IHC";
        }
        else if(technique.toLowerCase().equals("fish")){
            return "IHC";
        }

        return null;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

}
