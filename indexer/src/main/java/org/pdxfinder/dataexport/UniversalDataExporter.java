package org.pdxfinder.dataexport;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.pdxfinder.graph.dao.*;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;


/*
 * Created by csaba on 02/10/2019.
 */
@Component
public class UniversalDataExporter {

    private final static Logger log = LoggerFactory.getLogger(UniversalDataExporter.class);

    protected UtilityService utilityService;

    protected DataImportService dataImportService;

    protected String templateDir;

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

    private List<List<String>> mutationSheetData;

    private Group ds;


    public UniversalDataExporter(DataImportService dataImportService, UtilityService utilityService) {

        this.templateDir = templateDir;
        this.dataImportService = dataImportService;
        this.utilityService = utilityService;

        patientSheetData = new ArrayList<>();
        patientTumorSheetData = new ArrayList<>();
        pdxModelSheetData = new ArrayList<>();
        pdxModelValidationSheetData = new ArrayList<>();
        sharingAndContactSheetData = new ArrayList<>();
        loaderRelatedDataSheetData = new ArrayList<>();

        samplePlatformDescriptionSheetData = new ArrayList<>();

        mutationSheetData = new ArrayList<>();
    }


    public Group getDs() {
        return ds;
    }

    public void setDs(Group ds) {
        this.ds = ds;
    }

    public String getTemplateDir() {
        return templateDir;
    }

    public void setTemplateDir(String templateDir) {
        this.templateDir = templateDir;
    }

    public void export(String exportDir){

        //:: Methods to initialize the data lists

        /*
        initPatientData();
        initPatientTumorAtCollection();
        initPdxModelDetails();
        initPdxModelValidations();
        initSharingAndContact();
        initLoaderRelatedData();

        initSamplePlatformDescription();
*/
        initMutationData();



        //get the templates with the headers
        Workbook metadataWorkbook = getWorkbook(templateDir+"/metadata_template.xlsx");
        Workbook samplePlatformWorkbook = getWorkbook(templateDir+"/sampleplatform_template.xlsx");

        //insert data from the datalists to the template
        if(metadataWorkbook != null){

            updateSheetWithData(metadataWorkbook.getSheetAt(1), patientSheetData);
            updateSheetWithData(metadataWorkbook.getSheetAt(2), patientTumorSheetData);
            updateSheetWithData(metadataWorkbook.getSheetAt(3), pdxModelSheetData);
            updateSheetWithData(metadataWorkbook.getSheetAt(4), pdxModelValidationSheetData);
            updateSheetWithData(metadataWorkbook.getSheetAt(5), sharingAndContactSheetData);
            updateSheetWithData(metadataWorkbook.getSheetAt(6), loaderRelatedDataSheetData);
        }

        if(samplePlatformWorkbook != null){

            updateSheetWithData(samplePlatformWorkbook.getSheetAt(0), samplePlatformDescriptionSheetData);
        }


        // Write the output to a new file
        FileOutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream(exportDir+"/metadata_export.xlsx");
            metadataWorkbook.write(fileOut);
            fileOut.close();

            fileOut = new FileOutputStream(exportDir+"/sampleplatform_export.xlsx");
            samplePlatformWorkbook.write(fileOut);
            fileOut.close();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        System.out.println();
    }







    private Workbook getWorkbook(String templatePath) {

        File file = new File(templatePath);
        log.debug("Loading template {}", templatePath);
        if (!file.exists()) {
            log.error("Template not found "+templatePath);
            return null;
        }


        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
            return workbook;

        } catch (IOException e) {
            log.error("There was a problem accessing the file: {}", e);
        }
        return null;
    }


    private void initPatientData() {

        if (ds == null) return;

        List<Patient> patients = dataImportService.findPatientsByGroup(ds);

        for (Patient patient : patients) {

            List<String> dataRow = new ArrayList<>();

            String patientId = patient.getExternalId();
            String sex = patient.getSex();
            String cancerHistory = patient.getCancerRelevantHistory();
            String ethnicity = patient.getEthnicity();
            String firstDiagnosis = patient.getFirstDiagnosis();
            String ageAtFirstDiagnosis = patient.getAgeAtFirstDiagnosis();

            dataRow.add(patientId);
            dataRow.add(sex);
            dataRow.add(cancerHistory);
            dataRow.add(ethnicity);
            dataRow.add(firstDiagnosis);
            dataRow.add(ageAtFirstDiagnosis);

            patientSheetData.add(dataRow);
        }
    }

    private void initPatientTumorAtCollection(){

        if (ds == null) return;

        List<Patient> patients = dataImportService.findPatientTumorAtCollectionDataByDS(ds);

        for(Patient patient : patients){

            List<String> dataRow = new ArrayList<>();

            String patientId = patient.getExternalId();

            for(PatientSnapshot patientSnapshot : patient.getSnapshots()){

                for(Sample sample : patientSnapshot.getSamples()){

                    String sampleId = sample.getSourceSampleId();
                    String collectionDate = patientSnapshot.getDateAtCollection();
                    String collectionEvent = patientSnapshot.getCollectionEvent();
                    String elapsedTime = patientSnapshot.getElapsedTime();
                    String ageAtCollection = patientSnapshot.getAgeAtCollection();
                    String diagnosis = sample.getDiagnosis();
                    String tumorType = sample.getType().getName();
                    String primarySite = sample.getOriginTissue().getName();
                    String collectionSite = sample.getSampleSite().getName();
                    String stage = sample.getStage();
                    String stageClassification = sample.getStageClassification();
                    String grade = sample.getGrade();
                    String gradeClassification = sample.getGradeClassification();
                    String virologyStatus = patientSnapshot.getVirologyStatus();
                    String isPatientTreatmentInfoAvailable = "";
                    String treatmentNaive = patientSnapshot.getTreatmentNaive();
                    String isPatientTreated = "";
                    String wasPatientTreated = "";
                    String modelId = "";
                    try {
                        modelId = dataImportService.findModelBySample(sample).getSourcePdxId();
                    }
                    catch (Exception e){
                        log.error("Sample: "+sampleId);
                    }

                    dataRow.add(patientId);
                    dataRow.add(sampleId);
                    dataRow.add(collectionDate);
                    dataRow.add(collectionEvent);
                    dataRow.add(elapsedTime);
                    dataRow.add(ageAtCollection);
                    dataRow.add(diagnosis);
                    dataRow.add(tumorType);
                    dataRow.add(primarySite);
                    dataRow.add(collectionSite);
                    dataRow.add(stage);
                    dataRow.add(stageClassification);
                    dataRow.add(grade);
                    dataRow.add(gradeClassification);
                    dataRow.add(virologyStatus);
                    dataRow.add(isPatientTreatmentInfoAvailable);
                    dataRow.add(treatmentNaive);
                    dataRow.add(isPatientTreated);
                    dataRow.add(wasPatientTreated);
                    dataRow.add(modelId);

                    patientTumorSheetData.add(dataRow);
                }
            }
        }

    }



    private void initPdxModelDetails(){

        if (ds == null) return;


        List<ModelCreation> models = dataImportService.findModelsWithSpecimensAndQAByDS(ds.getAbbreviation());

        for(ModelCreation model : models){

            String modelId = model.getSourcePdxId();

            String pubmedIDs = "";
            if(model.getGroups() != null){

                for(Group g : model.getGroups()){

                    if(g.getType().equals("Publication")){

                        if(!pubmedIDs.equals("")){
                            pubmedIDs += ",";
                        }

                        pubmedIDs += g.getPubMedId();
                    }
                }
            }



            Map<String, ModelDetails> specimenMap = new HashMap<>();

            for(Specimen specimen : model.getSpecimens()){


                String passage = specimen.getPassage();

                String hostStrainName = specimen.getHostStrain().getName();
                String hostStrainNomenclature = specimen.getHostStrain().getSymbol();

                String engraftmentSite = "Not Specified";

                if(specimen.getEngraftmentSite() != null ){
                    engraftmentSite = specimen.getEngraftmentSite().getName();
                }

                String engraftmentType ="Not Specified";

                if(specimen.getEngraftmentType() != null){
                   engraftmentType = specimen.getEngraftmentType().getName();
                }

                String engraftmentMaterial ="Not Specified";
                String engraftmentMaterialStatus = "Not Specified";

                if(specimen.getEngraftmentMaterial() != null){
                    engraftmentMaterial = specimen.getEngraftmentMaterial().getName();
                    engraftmentMaterialStatus = specimen.getEngraftmentMaterial().getState();
                }


                String specimenMapKey = hostStrainName + hostStrainNomenclature + engraftmentSite + engraftmentType + engraftmentMaterial + engraftmentMaterialStatus;


                if(specimenMap.containsKey(specimenMapKey)){

                    specimenMap.get(specimenMapKey).getPassages().add(passage);
                }
                else{

                    ModelDetails md = new ModelDetails(hostStrainName, hostStrainNomenclature, engraftmentSite, engraftmentType, engraftmentMaterial, engraftmentMaterialStatus, passage);
                    specimenMap.put(specimenMapKey, md);
                }
            }

            for(Map.Entry<String, ModelDetails> entry : specimenMap.entrySet()){

                ModelDetails md = entry.getValue();
                List<String> dataRow = new ArrayList<>();

                dataRow.add(modelId);
                dataRow.add(md.getHostStrainName());
                dataRow.add(md.getHostStrainNomenclature());
                dataRow.add(md.getEngraftmentSite());
                dataRow.add(md.getEngraftmentType());
                dataRow.add(md.getEngraftmentMaterial());
                dataRow.add(md.getEngraftmentMaterialStatus());
                dataRow.add(md.getSortedPassages());
                dataRow.add(pubmedIDs);

                pdxModelSheetData.add(dataRow);
            }
        }
    }

    private void initPdxModelValidations(){

        if (ds == null) return;

        List<ModelCreation> models = dataImportService.findModelsWithSpecimensAndQAByDS(ds.getAbbreviation());

        for(ModelCreation model : models){

            String modelId = model.getSourcePdxId();

            for(QualityAssurance qa : model.getQualityAssurance()){

                List<String> dataRow = new ArrayList<>();

                String validationTechnique = qa.getTechnology();
                String validationDescription = qa.getDescription();
                String passages = qa.getPassages();
                String nomenclature = qa.getValidationHostStrain();

                dataRow.add(modelId);
                dataRow.add(validationTechnique);
                dataRow.add(validationDescription);
                dataRow.add(passages);
                dataRow.add(nomenclature);

                pdxModelValidationSheetData.add(dataRow);

            }
        }
    }


    private void initSharingAndContact(){

        if (ds == null) return;

        List<ModelCreation> models = dataImportService.findModelsWithSharingAndContactByDS(ds.getAbbreviation());

        for(ModelCreation model : models){


            String modelId = model.getSourcePdxId();

            Group providerGroup  = ds;
            Group accessGroup = null;
            Group projectGroup = null;

            if(model.getGroups() != null){

                for(Group g : model.getGroups()){

                    if(g.getType().equals("Provider")){
                        providerGroup = g;
                    }
                    else if(g.getType().equals("Project")){
                        projectGroup = g;
                    }
                    else if(g.getType().equals("Accessibility")){
                        accessGroup = g;
                    }
                }

            }


            String providerType = "";
            String providerName = "";
            String providerAbbrev = "";

            if(providerGroup != null){

                providerType = providerGroup.getProviderType();
                providerName = providerGroup.getName();
                providerAbbrev = providerGroup.getAbbreviation();
            }

            String modelAccessibility = "";
            String accessModalities = "";

            if(accessGroup != null){

                modelAccessibility = accessGroup.getAccessibility();
                accessModalities = accessGroup.getAccessModalities();
            }

            String projectName = "";

            if(projectGroup != null){

                projectName = projectGroup.getName();
            }

            String contactEmail = "";
            String contactName = "";
            String contactLink = "";
            String modelLink = "";

            for(ExternalUrl ex: model.getExternalUrls()){

                if(ex.getType().equals("contact")){

                    if(ex.getUrl()!= null && ex.getUrl().contains("@")){
                        contactEmail = ex.getUrl();
                    }
                    else{
                        contactLink = ex.getUrl();
                    }
                }


                if(ex.getType().equals("source")){

                    if(ex.getUrl()!= null){
                        modelLink = ex.getUrl();
                    }
                }
            }


            List<String> dataRow = new ArrayList<>();

            dataRow.add(modelId);
            dataRow.add(providerType);
            dataRow.add(modelAccessibility);
            dataRow.add(accessModalities);
            dataRow.add(contactEmail);
            dataRow.add(contactName);
            dataRow.add(contactLink);
            dataRow.add(modelLink);
            dataRow.add(providerName);
            dataRow.add(providerAbbrev);
            dataRow.add(projectName);

            sharingAndContactSheetData.add(dataRow);
        }
    }


    private void initLoaderRelatedData(){

        if (ds == null) return;

        List<String> dataRow = new ArrayList<>();
        dataRow.add(ds.getName());
        dataRow.add(ds.getAbbreviation());
        dataRow.add(ds.getUrl());

        loaderRelatedDataSheetData.add(dataRow);

    }



    private void initSamplePlatformDescription(){

        if (ds == null) return;

        List<ModelCreation> models = dataImportService.findModelXenograftPlatformSampleByDS(ds.getAbbreviation());

        for(ModelCreation model : models){

            String modelId = model.getSourcePdxId();


            //get the patient sample related molchars first
            if(model.getSample() != null){

                if(model.getSample().getMolecularCharacterizations() != null){

                    for(MolecularCharacterization mc : model.getSample().getMolecularCharacterizations()){

                        List<String> dataRow = new ArrayList<>();

                        dataRow.add(model.getSample().getSourceSampleId());
                        dataRow.add("patient");
                        dataRow.add("NA");
                        dataRow.add("");
                        dataRow.add(modelId);
                        dataRow.add("");
                        dataRow.add("");

                        dataRow.add(mc.getType());
                        dataRow.add(mc.getPlatform().getName());
                        dataRow.add(mc.getTechnology());
                        dataRow.add("");
                        dataRow.add("");
                        dataRow.add("");
                        dataRow.add("");
                        dataRow.add("");
                        dataRow.add(mc.getPlatform().getUrl());

                        samplePlatformDescriptionSheetData.add(dataRow);
                    }
                }
            }

            //then add the xenograft related molchars

            if(model.getSpecimens() != null){

                for(Specimen sp : model.getSpecimens()){

                    String passage = sp.getPassage();

                    String hostStrainName = "";
                    String hostStrainNomenclature = "";

                    if(sp.getHostStrain() != null){

                        hostStrainName = (sp.getHostStrain().getName() == null?"":sp.getHostStrain().getName());
                        hostStrainNomenclature = (sp.getHostStrain().getSymbol() == null?"":sp.getHostStrain().getSymbol());
                    }


                    Sample sample = sp.getSample();

                    for(MolecularCharacterization mc : sample.getMolecularCharacterizations()){

                        List<String> dataRow = new ArrayList<>();

                        dataRow.add(sample.getSourceSampleId());
                        dataRow.add("xenograft");
                        dataRow.add(passage);
                        dataRow.add("");
                        dataRow.add(modelId);
                        dataRow.add(hostStrainName);
                        dataRow.add(hostStrainNomenclature);

                        dataRow.add(mc.getType());
                        dataRow.add(mc.getPlatform().getName());
                        dataRow.add(mc.getTechnology());
                        dataRow.add("");
                        dataRow.add("");
                        dataRow.add("");
                        dataRow.add("");
                        dataRow.add("");
                        dataRow.add(mc.getPlatform().getUrl());

                        samplePlatformDescriptionSheetData.add(dataRow);
                    }
                }
            }
        }

    }


    private void initMutationData(){

        String molcharType = "mutation";

        List<ModelCreation> models = dataImportService.findModelsWithSharingAndContactByDS(ds.getAbbreviation());

        for(ModelCreation m: models){

            ModelCreation model = dataImportService.findModelWithMolecularDataByDSAndIdAndMolcharType(ds.getAbbreviation(), m.getSourcePdxId(), molcharType);

            String modelId = model.getSourcePdxId();

            //check for molchars on patient sample
            if(model.getSample() != null && model.getSample().getMolecularCharacterizations() != null){

                String sampleId = model.getSample().getSourceSampleId();

                for(MolecularCharacterization mc : model.getSample().getMolecularCharacterizations()){

                    insertOmicDataToSheet(modelId, sampleId,"patient", molcharType, null, mc, mutationSheetData);
                }
            }

            //then look for xenograft molchars
            if(model.getSpecimens()!= null){

                for(Specimen sp : model.getSpecimens()){

                    if(sp.getSample() != null && sp.getSample().getMolecularCharacterizations() != null){

                        String sampleId = sp.getSample().getSourceSampleId();

                        for(MolecularCharacterization mc: sp.getSample().getMolecularCharacterizations()){

                            insertOmicDataToSheet(modelId, sampleId,"xenograft", molcharType, sp, mc, mutationSheetData);
                        }
                    }
                }
                log.info("");
            }



        }
    }







    private void insertOmicDataToSheet(String modelId, String sampleId, String sampleOrigin, String molcharType, Specimen specimen, MolecularCharacterization mc, List<List<String>> sheetData){

        for(MarkerAssociation ma: mc.getMarkerAssociations()){

            List<String> rowData = new ArrayList<>();

            rowData.add(modelId);
            rowData.add(sampleId);
            rowData.add(sampleOrigin);

            if(sampleOrigin.equals("patient")){
                //no passage, host strain for patient samples
                rowData.add("");
                rowData.add("");
            }
            else{

                rowData.add(specimen.getHostStrain().getSymbol());
                rowData.add(specimen.getPassage());
            }

            //then get the MA data inserted

            if(molcharType.equals("mutation")){

                rowData.add(ma.getAminoAcidChange());
                rowData.add(ma.getNucleotideChange());
                rowData.add(ma.getConsequence());
                rowData.add(ma.getReadDepth());
                rowData.add(ma.getAlleleFrequency());
                rowData.add(ma.getChromosome());
                rowData.add(ma.getSeqStartPosition());
                rowData.add(ma.getRefAllele());
                rowData.add(ma.getAltAllele());
                rowData.add(ma.getMarker().getUcscGeneId());
                rowData.add(ma.getMarker().getNcbiGeneId());
                rowData.add(ma.getMarker().getEnsemblGeneId());
                //no transcript id
                rowData.add("");
                rowData.add(ma.getRsIdVariants());
                rowData.add(ma.getGenomeAssembly());
                rowData.add(mc.getPlatform().getName());

            }else if(molcharType.equals("copy number alteration")){


            }

            sheetData.add(rowData);






        }

    }

    private void updateSheetWithData(Sheet sheet, List<List<String>> data){

        int startRow = 6;

        for(int i = 0; i < data.size(); i++){

            int rowIndex = startRow + i - 1;
            sheet.createRow(rowIndex);

            for(int j = 0; j < data.get(i).size(); j++){


                int columnIndex = j +1;
                sheet.getRow(rowIndex).createCell(columnIndex);


                Cell cell = null;
                try{
                    cell = sheet.getRow(rowIndex).getCell(columnIndex);
                    cell.setCellValue(data.get(i).get(j));
                }
                catch (Exception e){

                    log.error("Exception in "+sheet.getSheetName()+" :"+ rowIndex+":"+columnIndex);
                }
            }
        }

    }


}
