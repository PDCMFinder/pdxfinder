package org.pdxfinder.dataloaders;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.pdxfinder.graph.dao.*;
import org.pdxfinder.reportmanager.ReportManager;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.pdxfinder.services.ds.Standardizer;
import org.pdxfinder.services.dto.NodeSuggestionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Univeral Loader, aka UPDOG: Universal PdxData tO Graph
 *
 * "What's UPDOG?"
 * "Nothing much, what's up with you?"
 * Comments may contain egregious dog puns.
 */
@Component
public class UniversalLoader extends UniversalLoaderOmic {

    private final static Logger log = LoggerFactory.getLogger(UniversalLoader.class);
    static ApplicationContext context;
    ReportManager reportManager;

    private String finderRootDir;
    private String dataRootDirectory;

    private List<List<String>> patientSheetData;
    private List<List<String>> patientTumorSheetData;
    private List<List<String>> patientTreatmentSheetData;
    private List<List<String>> pdxModelSheetData;
    private List<List<String>> pdxModelValidationSheetData;
    private List<List<String>> samplePlatformDescriptionSheetData;
    private List<List<String>> sharingAndContactSheetData;
    private List<List<String>> cytogeneticsSheetData;
    private List<List<String>> loaderRelatedDataSheetData;
    private List<List<String>> drugDosingSheetData;

    private Group ds;
    private Boolean stopLoading;
    private Set<String> modelIDs;

    public UniversalLoader(ReportManager reportManager, UtilityService utilityService, DataImportService dataImportService) {
        super(utilityService, dataImportService);
        this.reportManager = reportManager;
    }


    public void initTemplates(String updogCurrDir) throws Exception {

        log.info("******************************************************");
        log.info("* Initializing Sheet data                            *");
        log.info("******************************************************");

        ds = null;
        stopLoading = false;

        patientSheetData = new ArrayList<>();
        patientTumorSheetData = new ArrayList<>();
        pdxModelSheetData = new ArrayList<>();
        pdxModelValidationSheetData = new ArrayList<>();
        sharingAndContactSheetData = new ArrayList<>();
        loaderRelatedDataSheetData = new ArrayList<>();
        Optional<Workbook> metadata = getWorkbook(updogCurrDir, "metadata.xlsx");
        if (metadata.isPresent()) {
            initializeSheetData(metadata.get().getSheetAt(1), patientSheetData);
            initializeSheetData(metadata.get().getSheetAt(2), patientTumorSheetData);
            initializeSheetData(metadata.get().getSheetAt(3), pdxModelSheetData);
            initializeSheetData(metadata.get().getSheetAt(4), pdxModelValidationSheetData);
            initializeSheetData(metadata.get().getSheetAt(5), sharingAndContactSheetData);
            initializeSheetData(metadata.get().getSheetAt(6), loaderRelatedDataSheetData);
        }

        samplePlatformDescriptionSheetData = new ArrayList<>();
        Optional<Workbook> samplePlatformDescription = getWorkbook(updogCurrDir, "sampleplatform.xlsx");
        if (samplePlatformDescription.isPresent()) {
            initializeSheetData(samplePlatformDescription.get().getSheetAt(0), samplePlatformDescriptionSheetData);
        }

        cytogeneticsSheetData = new ArrayList<>();
        Optional<Workbook> cytogenetics = getWorkbook(updogCurrDir, "cyto/cytogenetics.xlsx");
        if (cytogenetics.isPresent()) {
            initializeSheetData(cytogenetics.get().getSheetAt(0), cytogeneticsSheetData);
        }

        patientTreatmentSheetData = new ArrayList<>();
        Optional<Workbook> patientTreatment = getWorkbook(updogCurrDir, "treatment/patienttreatment.xlsx");
        if (patientTreatment.isPresent()) {
            initializeSheetData(patientTreatment.get().getSheetAt(0), patientSheetData);
        }

        drugDosingSheetData = new ArrayList<>();
        Optional<Workbook> drugDosing = getWorkbook(updogCurrDir, "treatment/drugdosing.xlsx");
        if (drugDosing.isPresent()) {
            initializeSheetData(drugDosing.get().getSheetAt(0), drugDosingSheetData);
        }
    }


    public void loadTemplateData() {

        //:: DON'T CHANGE THE ORDER OF THESE METHODS UNLESS YOU WANT TO RISK THE UNIVERSE TO COLLAPSE!
        createDataSourceGroup();
        createPatients();
        createPatientTumors();
        createPdxModelDetails();
        createPdxModelValidations();
        createSharingAndContacts();
        createDerivedPatientModelDataset();
        createPatientTreatments();
        createOmicData();
        createCytogeneticsData();

    }

    public Optional<Workbook> getWorkbook(String updogCurrDir, String templatePath) {
        templatePath = String.join("/", updogCurrDir, templatePath);
        File file = new File(templatePath);
        log.debug("Loading template {}", templatePath);
        if (!file.exists()) return Optional.empty();

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
            return Optional.of(workbook);

        } catch (IOException e) {
            log.error("There was a problem accessing the file: {}", e);
        }
        return Optional.empty();
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
     * Creates the provider group in the database
     */
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

        ds = dataImportService.getProviderGroup(providerName, providerAbbreviation, "", "", "", sourceUrl);

    }

    /**
     * Creates the patient nodes
     */
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

    /**
     * Targets an existing patient, creates patient snapshots, patient sample, tumor type and the model
     */
    private void createPatientTumors() {

        if (stopLoading) return;

        log.info("******************************************************");
        log.info("* Creating Patient samples and snapshots             *");
        log.info("******************************************************");

        int row = 6;

        log.info("Tumor row number: " + patientTumorSheetData.size());
        for (List<String> patientTumorRow : patientTumorSheetData) {

            String patientId = null;
            String modelId = null;
            String dateOfCollection = null;
            String ageAtCollection = null;
            String collectionEvent = null;
            String elapsedTime = null;

            try {
                patientId = patientTumorRow.get(0);
                String sampleId = patientTumorRow.get(1);
                modelId = patientTumorRow.get(19);

                //skip rows where patient, model or sample id is null
                if (patientId == null || sampleId == null || modelId == null) continue;

                dateOfCollection = patientTumorRow.get(2);
                collectionEvent = patientTumorRow.get(3);
                elapsedTime = patientTumorRow.get(4);
                ageAtCollection = patientTumorRow.get(5);
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


                if (modelId == null) {
                    log.error("Missing corresponding Model ID in row " + row);
                    row++;
                    continue;
                }

                if (dateOfCollection == null && collectionEvent == null && elapsedTime == null && ageAtCollection == null) {
                    log.error("Missing collection info  in row " + row);
                    row++;
                    continue;
                }

                //hack to avoid 0.0 values and negative numbers
                if (elapsedTime != null) {

                    elapsedTime = elapsedTime.replaceAll("[^0-9]", "");
                }


                Patient patient = dataImportService.getPatientWithSnapshots(patientId, ds);

                if (patient == null) {

                    log.error("Patient does not exist, can not create tumor for " + patientId);
                    row++;
                    continue;
                }

                //need this trick to remove float values, ie: patient age = 30.0
                if (ageAtCollection != null && !ageAtCollection.equals("Not Specified")) {
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
                log.error("Exception in row: " + row + " for model: " + modelId);
                log.error("doc:" + dateOfCollection + " ce:" + collectionEvent + " et:" + elapsedTime + " aac:" + ageAtCollection);
                e.printStackTrace();

            }

        }

    }

    /**
     * Targets an existing patient and snapshot to create a treatment summary with treatment protocols
     */
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
            String response = patientTreatmentRow.get(7);
            String responseClassification = patientTreatmentRow.get(8);

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

    /**
     * Targets an existing model to create specimens with engraftment site, type and material, host strain as well as publication groups
     */
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

            } catch (Exception e) {
                log.error("Error with model: " + modelId + " Probably duplicates?");
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
            try {
                hostStrain = dataImportService.getHostStrain(hostStrainName, hostStrainNomenclature, "", "");
            } catch (Exception e) {
                e.printStackTrace();
            }

            // passage = 1,3,5
            if (passage.contains(",")) {

                String[] passageArr = passage.split(",");

                for (int i = 0; i < passageArr.length; i++) {

                    //create specimens with engraftment data
                    Specimen specimen = new Specimen();
                    specimen.setPassage(passageArr[i].trim());
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

            } else if (passage.matches("[+-]?([0-9]*[.])?[0-9]+")) {

                //need this trick to get rid of fractures if there is any
                double passageDouble = Double.parseDouble(passage);
                passage = String.valueOf((int)passageDouble);

                //create specimens with engraftment data
                Specimen specimen = new Specimen();
                specimen.setPassage(passage);
                specimen.setEngraftmentSite(es);
                specimen.setEngraftmentType(et);
                specimen.setEngraftmentMaterial(em);
                specimen.setHostStrain(hostStrain);

                model.addSpecimen(specimen);
            } else {

                log.error("Not supported value(" + passage + ") for passage at row " + row);
            }

            //CREATE PUBLICATION GROUPS

            //check if pubmed id is in the right format, ie id starts with PMID
            if (pubmedIdString != null && !pubmedIdString.isEmpty() && pubmedIdString.toLowerCase().contains("pmid")) {

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

    /**
     * Targets existing model to add validation (QA) nodes
     */
    private void createPdxModelValidations() {

        if (stopLoading) return;

        log.info("******************************************************");
        log.info("* Creating Model validations                         *");
        log.info("******************************************************");

        int row = 6;

        for (List<String> pdxModelValidationRow : pdxModelValidationSheetData) {

            String modelId = "";
            String validationTechnique = "";
            String validationDescription = "";
            String passages = "";
            String validationHostStrain = "";

            try {
                modelId = pdxModelValidationRow.get(0);
                validationTechnique = pdxModelValidationRow.get(1);
                validationDescription = pdxModelValidationRow.get(2);
                passages = pdxModelValidationRow.get(3);
                validationHostStrain = pdxModelValidationRow.get(4);
            } catch (Exception e) {

                log.error("Error in row: " + row);
                e.printStackTrace();
            }

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

                String pass;
                try {
                    int passageInt = (int) Float.parseFloat(passageArr[i]);
                    pass = String.valueOf(passageInt);
                } catch (NumberFormatException | NullPointerException nfe) {

                    pass = passageArr[i];
                }

                passages += pass + ",";
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

    /**
     * Requirements:
     * <p>
     * PATIENT
     * existing patient sample
     * <p>
     * XENOGRAFT
     * existing model, specimen, creates xeno sample if not present
     * <p>
     * Creates a molecular characterization with a platform and links it to the appropriate sample
     */
    private void createDerivedPatientModelDataset() {

        if (stopLoading) return;

        log.info("******************************************************");
        log.info("* Creating dataset derived from patients and models  *");
        log.info("******************************************************");

        int row = 6;
        this.modelIDs = new HashSet<>();

        for (List<String> derivedDatasetRow : samplePlatformDescriptionSheetData) {

            String sampleId = derivedDatasetRow.get(0);

            String origin = derivedDatasetRow.get(1);
            String passage = derivedDatasetRow.get(2);
            String engraftedTumorCollectionSite = derivedDatasetRow.get(3);
            String modelId = derivedDatasetRow.get(4);
            String hostStrainName = derivedDatasetRow.get(5);

            String nomenclature = derivedDatasetRow.get(6);

            String molCharType = derivedDatasetRow.get(7);
            String platformName = derivedDatasetRow.get(8);
            String platformTechnology = derivedDatasetRow.get(9);
            String platformDescription = derivedDatasetRow.get(10);
            String analysisProtocol = derivedDatasetRow.get(11);

            String platformUrl = derivedDatasetRow.get(15);


            if (platformUrl != null) {
                platformUrl = platformUrl.replaceAll("[^A-Za-z0-9 /_-]", "");
            }


            //TODO: get additional fields from the sheet

            //check essential values

            if (sampleId == null || origin == null || modelId == null
                    || molCharType == null || platformName == null || platformTechnology == null || platformDescription == null
                    ) {

                log.error("Missing essential value in row " + row);
                row++;
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

                    if ((platform.getUrl() == null || platform.getUrl().isEmpty()) && platformUrl != null && platformUrl.length() > 3) {
                        log.info("Saved platform:" + platform.getName() + " Url: " + (platform.getUrl() == null ? "null" : platform.getUrl()) + " Url in file: " + platformUrl);
                        platform.setUrl(platformUrl);
                        dataImportService.savePlatform(platform);
                        log.info("Updating platform url");
                    } else {

                        log.warn("Platform " + platform.getName() + " was not updated. ");
                    }

                    MolecularCharacterization mc = new MolecularCharacterization();
                    mc.setPlatform(platform);
                    mc.setType(molCharType.toLowerCase());
                    mc.setTechnology(platformTechnology);
                    sample.addMolecularCharacterization(mc);
                    sample.setSourceSampleId(sampleId);
                    dataImportService.saveSample(sample);

                } else {

                    log.error("Unknown human sample with id: " + sampleId);
                    row++;
                    continue;
                }


            }


            //xenograft sample
            //specimen should have been created before
            else if (origin.toLowerCase().equals("engrafted tumor") || origin.toLowerCase().equals("engrafted tumour") || origin.toLowerCase().equals("xenograft")) {

                if (passage == null || passage.isEmpty() || passage.toLowerCase().equals("not specified")) {

                    log.error("Missing essential value Xenograft Passage in row " + row);
                    row++;
                    continue;
                }

                if (nomenclature == null || nomenclature.isEmpty()) {

                    log.error("Missing essential value nomenclature in row " + row);
                    row++;
                    continue;
                }

                //need this trick to get rid of 0.0 if there is any
                //if(passage.equals("0.0")) passage = "0";
                int passageInt = (int) Float.parseFloat(passage);
                passage = String.valueOf(passageInt);

                model = dataImportService.findModelByIdAndDataSourceWithSpecimensAndHostStrain(modelId, ds.getAbbreviation());

                if (model == null) {
                    log.error("Model " + modelId + " not found, skipping");
                    row++;
                    continue;
                }

                //check if targeted specimen is present, if not, create it
                Specimen specimen = null;

                for (Specimen sp : model.getSpecimens()) {
                    //specimen passage and host strain nomenclature are the same
                    if (sp.getPassage().equals(passage) && sp.getHostStrain().getSymbol().equals(nomenclature)) {

                        specimen = sp;
                        break;
                    }
                }

                //this is a new specimen
                if (specimen == null) {

                    HostStrain hostStrain = null;

                    try {
                        hostStrain = dataImportService.getHostStrain(hostStrainName, nomenclature, null, null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    specimen = new Specimen();
                    specimen.setPassage(passage);
                    specimen.setHostStrain(hostStrain);

                    Sample s = new Sample();
                    s.setSourceSampleId(sampleId);
                    specimen.setSample(s);
                    model.addSpecimen(specimen);

                }


                sample = specimen.getSample();

                if (sample == null) {

                    sample = new Sample();
                    sample.setSourceSampleId(sampleId);

                }

                platform = dataImportService.getPlatform(platformName, ds);


                if ((platform.getUrl() == null || platform.getUrl().isEmpty()) && platformUrl != null && platformUrl.length() > 3) {
                    log.info("Saved platform:" + platform.getName() + " Url: " + (platform.getUrl() == null ? "null" : platform.getUrl()) + " Url in file: " + platformUrl);
                    platform.setUrl(platformUrl);
                    dataImportService.savePlatform(platform);
                    log.info("Updating platform url");
                }

                MolecularCharacterization mc = new MolecularCharacterization();
                mc.setPlatform(platform);
                mc.setType(molCharType.toLowerCase());
                mc.setTechnology(platformTechnology);
                sample.addMolecularCharacterization(mc);
                model.addRelatedSample(sample);

                specimen.setSample(sample);

                model.addSpecimen(specimen);
                model.addRelatedSample(sample);
                dataImportService.saveModelCreation(model);
                dataImportService.saveSpecimen(specimen);
                dataImportService.saveSample(sample);

                //log.info(" Specimen with the following details was created: "+modelId+" "+passage+" "+nomenclature+ " in row: "+row);


            } else {
                //origin is not patient nor xenograft
                log.error("Unknown sample origin in row " + row);
            }

            this.modelIDs.add(modelId);
            row++;
        }

    }


    private void createOmicData() {

        log.info("******************************************************");
        log.info("*                 Loading Omic Data                  *");
        log.info("******************************************************");
        log.info(this.modelIDs.toString());

        omicDataSource = ds.getAbbreviation();
        dataSourceAbbreviation = loaderRelatedDataSheetData.get(0).get(1);
        finderRootDir = stripTrailingSlash(finderRootDir);
        dataRootDirectory = finderRootDir + "/data/UPDOG";

        omicModelID = "Model_ID";
        omicSampleID = "Sample_ID";
        omicSampleOrigin = "sample_origin";
        omicPassage = "Passage";
        omicHostStrainName = "host_strain_name";
        omicHgncSymbol = "hgnc_symbol";
        omicAminoAcidChange = "amino_acid_change";
        omicNucleotideChange = "nucleotide_change";
        omicConsequence = "consequence";
        omicReadDepth = "read_depth";
        omicAlleleFrequency = "Allele_frequency";
        omicChromosome = "chromosome";
        omicSeqStartPosition = "seq_start_position";
        omicRefAllele = "ref_allele";
        omicAltAllele = "alt_allele";
        omicUcscGeneId = "ucsc_gene_id";
        omicNcbiGeneId = "ncbi_gene_id";
        omicEnsemblGeneId = "ensembl_gene_id";
        omicEnsemblTranscriptId = "ensembl_transcript_id";
        omicRsIdVariants = "rs_id_Variant";
        omicGenomeAssembly = "genome_assembly";
        omicPlatform = "Platform";

        omicSeqEndPosition = "seq_end_position";
        omicCnaLog10RCNA = "log10R_cna";
        omicCnaLog2RCNA = "log2R_cna";
        omicCnaCopyNumberStatus = "copy_number_status";
        omicCnaGisticvalue = "gistic_value_cna";
        omicCnaPicnicValue = "picnic_value";
        rnaSeqCoverage = "RNAseq_coverage";
        rnaSeqFPKM = "RNAseq_FPKM";
        rnaSeqTPM = "RNAseq_TPM";
        rnaSeqCount = "RNAseq_count";
        affyHGEAProbeId = "affy_HGEA_probeID";
        affyHGEAExpressionValue = "Affy_HGEA_expressionValue";
        illuminaHGEAProbeId = "Illumina_HGEA_probeID";
        illuminaHGEAExpressionValue = "Illumina_HGEA_expressionValue";



        platformURL = new HashMap<>();
        platformURL.put("CGH_array", "/platform/curie-lc-cna/");
        platformURL.put("Targeted_NGS", "/platform/curie-lc-mutation/");

        if (dataSourceAbbreviation.equals("CRL")) {
            omicDataFilesType = "ONE_FILE_PER_MODEL";
            omicFileExtension = "csv";
        }
        else if(dataSourceAbbreviation.equals("UOM-BC")){
            omicDataFilesType = "ONE_FILE_PER_MODEL";
            omicFileExtension = "xlsx";
        }
        else {
            omicDataFilesType = "ALL_MODELS_IN_ONE_FILE";
            omicFileExtension = "xlsx";
        }


        String providerDataRootDir = dataRootDirectory + "/" +dataSourceAbbreviation;

        String mutationDataDir = dataRootDirectory + "/" + dataSourceAbbreviation + "/mut/";
        String cnaDataDir = dataRootDirectory + "/" + dataSourceAbbreviation + "/cna/";
        String transcriptomicDataDir = dataRootDirectory + "/" + dataSourceAbbreviation + "/trans/";

        File mutationData = new File(mutationDataDir);
        File cnaData = new File(cnaDataDir);
        File transcriptomicData = new File(transcriptomicDataDir);


        log.info("Provider data root dir: "+providerDataRootDir);
        for (String modelId : this.modelIDs) {

            ModelCreation modelCreation = dataImportService.findBySourcePdxIdAndDataSourceWithSamplesAndSpecimensAndHostStrain(modelId, ds.getAbbreviation());

            if (modelCreation != null) {

                // Mutation Data Load
                if (mutationData.exists()) {
                    log.info("Loading mutation for " + modelId);
                    loadOmicData(modelCreation, ds, "mutation", providerDataRootDir);
                }

                // Copy Number Alteration Data Load
                if (cnaData.exists()) {
                    log.info("Loading cna for " + modelId);
                    loadOmicData(modelCreation, ds, "copy number alteration", providerDataRootDir);
                }

                // Transcriptomics
                if(transcriptomicData.exists()){
                    log.info("Loading transcriptomics for "+modelId);
                    loadOmicData(modelCreation, ds, "transcriptomics", providerDataRootDir);
                }


            } else {

                log.error("Cannot load omic data for missing model: " + modelId);
            }

        }


    }


    /**
     * Targets existing model, creates external urls, updates provider type
     */
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

            if (accessModalities == null) accessModalities = "";
            if (modelAccessibility == null) modelAccessibility = "";

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

            if (modelAccessibility != "" || accessModalities != "") {

                Group access = dataImportService.getAccessibilityGroup(modelAccessibility, accessModalities);
                model.addGroup(access);
            }

            dataImportService.saveModelCreation(model);

            //Update datasource
            ds.setProviderType(dataProviderType);
            ds.setContact(contactEmail);


        }

        dataImportService.saveGroup(ds);

    }


    private void createCytogeneticsData() {

        if (stopLoading) return;

        log.info("******************************************************");
        log.info("* Creating breast and or colorectal markers          *");
        log.info("******************************************************");

        int row = 6;

        Map<String, MolecularCharacterization> patientMolChars = new HashMap<>();
        Map<String, MolecularCharacterization> xenoMolChars = new HashMap<>();

        //TODO: At some point deal with micro-satelite instability. Currently those rows are skipped. We don't want instability in our lives just yet.

        //first get all markers for the individual molchar objects
        for (List<String> dataRow : cytogeneticsSheetData) {

            String sampleId = dataRow.get(0);
            String origin = dataRow.get(1);
            String passage = dataRow.get(2);
            String nomenclature = dataRow.get(3);
            String modelId = dataRow.get(4);
            String markerSymbol = dataRow.get(5);
            String markerStatus = dataRow.get(6);
            String technique = dataRow.get(8);
            String platform = dataRow.get(9);
            String characterizationType = "Unknown";

            if (origin == null || modelId == null || markerSymbol == null || markerStatus == null || technique == null) {
                log.error("Missing essential value in row " + row);
                row++;
                continue;
            }

            MolecularCharacterization mc;
            Platform pl;
            Marker marker = null;

            if (technique.toLowerCase().equals("immunohistochemistry") || technique.toLowerCase().equals("fish")) {
                characterizationType = "cytogenetics";
            }

            NodeSuggestionDTO nsdto = dataImportService.getSuggestedMarker(this.getClass().getSimpleName(), ds.getAbbreviation(), modelId, markerSymbol, characterizationType, technique);

            if (nsdto.getNode() == null) {

                //uh oh, we found an unrecognised marker symbol, abort, abort!!!!
                reportManager.addMessage(nsdto.getLogEntity());
                continue;
            } else {

                marker = (Marker) nsdto.getNode();

                if (origin.toLowerCase().equals("patient")) {

                    //for patient related molchars it is sufficient to use the model + platform as the key
                    String mapKey = modelId + "___" + technique;

                    if (patientMolChars.containsKey(mapKey)) {
                        //get a previously created mc object = platform and type are already set
                        mc = patientMolChars.get(mapKey);
                    } else {
                        //new mc object, need to set the platform, too
                        pl = dataImportService.getPlatform(technique, ds);

                        mc = new MolecularCharacterization();
                        mc.setPlatform(pl);
                        mc.setType(getMolcharType(technique));
                    }


                    MarkerAssociation ma = new MarkerAssociation();
                    ma.setMarker(marker);

                    if (technique.toLowerCase().equals("immunohistochemistry") || technique.toLowerCase().equals("fish")) {

                        ma.setCytogeneticsResult(markerStatus);
                    }
                    //what if it is not ihc?

                    mc.addMarkerAssociation(ma);

                    //put molchar in the map if it was just created, but don't store molchars without type
                    if (!patientMolChars.containsKey(mapKey) && mc.getType() != null) {
                        patientMolChars.put(mapKey, mc);
                    }


                } else if (origin.toLowerCase().equals("xenograft") || origin.toLowerCase().equals("engrafted tumor") || origin.toLowerCase().equals("engrafted tumour")) {

                    //need this trick to get rid of 0.0 if there is any
                    int passageInt = (int) Float.parseFloat(passage);
                    passage = String.valueOf(passageInt);

                    //for xenograft molchars use the combination of the modelid, nomenclature, passage and technique as the key
                    String mapKey = modelId + "___" + nomenclature + "___" + passage + "___" + technique;

                    if (xenoMolChars.containsKey(mapKey)) {
                        //get a previously created mc object = platform and type are already set
                        mc = xenoMolChars.get(mapKey);
                    } else {
                        //new mc object, need to set the platform, too
                        pl = dataImportService.getPlatform(technique, ds);

                        mc = new MolecularCharacterization();
                        mc.setPlatform(pl);
                        mc.setType(getMolcharType(technique));

                    }


                    MarkerAssociation ma = new MarkerAssociation();
                    ma.setMarker(marker);

                    if (technique.toLowerCase().equals("immunohistochemistry") || technique.toLowerCase().equals("fish")) {

                        ma.setCytogeneticsResult(markerStatus);
                    }
                    //what if it is not ihc? Would our world collapse entirely?

                    mc.addMarkerAssociation(ma);
                    //but don't store molchars without type
                    if (!xenoMolChars.containsKey(mapKey) && mc.getType() != null) {
                        xenoMolChars.put(mapKey, mc);
                    }

                } else {
                    //origin is not patient nor xenograft
                    log.error("Unknown sample origin in row " + row);
                }

            }


            row++;
        }


        //get the corresponding samples for the molchar objects, link them and save them.
        //patient samples
        for (Map.Entry<String, MolecularCharacterization> entry : patientMolChars.entrySet()) {
            //key = model ID + "___" + platform
            String[] keyArr = entry.getKey().split("___");
            String key = keyArr[0];

            MolecularCharacterization mc = entry.getValue();

            Sample patientSample = dataImportService.findHumanSample(key, ds.getAbbreviation());

            if (patientSample != null) {

                patientSample.addMolecularCharacterization(mc);
                dataImportService.saveSample(patientSample);
            } else {

                log.error("Failed to create molchar for patient sample! Model:" + key);
            }

        }

        //xeno samples
        for (Map.Entry<String, MolecularCharacterization> entry : xenoMolChars.entrySet()) {
            //key =  modelId + "___" + nomenclature + "___" + passage + "___" + platform

            MolecularCharacterization mc = entry.getValue();
            String[] keyArr = entry.getKey().split("___");

            String modelId = keyArr[0];
            String nomenclature = keyArr[1];
            String passage = keyArr[2];

            ModelCreation model = dataImportService.findModelByIdAndDataSource(modelId, ds.getAbbreviation());
            if (model == null) {
                log.error("Cannot load markers, model not found: " + modelId);
                continue;

            }

            Specimen specimen = dataImportService.findSpecimenByModelAndPassageAndNomenclature(model, passage, nomenclature);

            if (specimen == null) {


                HostStrain hostStrain = dataImportService.findHostStrain(nomenclature);

                if (hostStrain != null) {

                    specimen = new Specimen();
                    specimen.setHostStrain(hostStrain);
                    specimen.setPassage(passage);

                } else {

                    log.error("Cannot save cytogenetics for Model: "+modelId +" Failed to get hoststrain "+nomenclature);
                    continue;
                }
            }

            Sample xenoSample = specimen.getSample();

            if (xenoSample == null) {
                xenoSample = new Sample();

            }

            xenoSample.addMolecularCharacterization(mc);
            specimen.setSample(xenoSample);
            model.addRelatedSample(xenoSample);
            dataImportService.saveSpecimen(specimen);
            dataImportService.saveModelCreation(model);

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


    private String getMolcharType(String technique) {

        if (technique.toLowerCase().equals("immunohistochemistry")) {
            return "cytogenetics";
        } else if (technique.toLowerCase().equals("fish")) {
            return "cytogenetics";
        }

        return null;
    }

    public static String stripTrailingSlash(String filePath) {
        return filePath.endsWith("/") ?
            filePath.replaceFirst("/$", "") :
            filePath;
    }


    public List<List<String>> getPatientSheetData() {
        return patientSheetData;
    }

    public List<List<String>> getPatientTumorSheetData() {
        return patientTumorSheetData;
    }

    public List<List<String>> getPatientTreatmentSheetData() {
        return patientTreatmentSheetData;
    }

    public List<List<String>> getPdxModelSheetData() {
        return pdxModelSheetData;
    }

    public List<List<String>> getPdxModelValidationSheetData() {
        return pdxModelValidationSheetData;
    }

    public List<List<String>> getSamplePlatformDescriptionSheetData() {
        return samplePlatformDescriptionSheetData;
    }

    public List<List<String>> getSharingAndContactSheetData() {
        return sharingAndContactSheetData;
    }

    public List<List<String>> getCytogeneticsSheetData() {
        return cytogeneticsSheetData;
    }

    public List<List<String>> getLoaderRelatedDataSheetData() {
        return loaderRelatedDataSheetData;
    }

    public String getFinderRootDir() {
        return finderRootDir;
    }

    public void setFinderRootDir(String finderRootDir) {
        this.finderRootDir = finderRootDir;
    }
}