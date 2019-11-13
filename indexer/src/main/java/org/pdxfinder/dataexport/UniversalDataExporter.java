package org.pdxfinder.dataexport;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.pdxfinder.graph.dao.*;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;


/*
 * Created by csaba on 02/10/2019.
 */

public class UniversalDataExporter {

    private static final Logger log = LoggerFactory.getLogger(UniversalDataExporter.class);

    protected UtilityService utilityService;

    protected DataImportService dataImportService;

    protected String templateDir;

    //data lists
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
    private List<List<String>> cnaSheetData;

    private Group ds;

    private static String notSpecified = "Not Specified";
    private static String patientOrigin = "patient";

    public UniversalDataExporter() {
    }

    public UniversalDataExporter(DataImportService dataImportService, UtilityService utilityService) {

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
        cnaSheetData = new ArrayList<>();
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

    public void init(String templateDir, Group ds){

        this.templateDir = templateDir;
        this.ds = ds;

        //:: Methods to initialize the data lists

        initPatientData();
        initPatientTumorAtCollection();
        initPdxModelDetails();
        initPdxModelValidations();
        initSharingAndContact();
        initLoaderRelatedData();

        initSamplePlatformDescription();

        initMutationData();
        initCNAData();

    }

    public void export(String exportDir){

        //get the templates with the headers
        XSSFWorkbook metadataWorkbook = getWorkbook(templateDir+"/metadata_template.xlsx");
        XSSFWorkbook samplePlatformWorkbook = getWorkbook(templateDir+"/sampleplatform_template.xlsx");
        XSSFWorkbook mutationWorkbook = getWorkbook(templateDir+"/mutation_template.xlsx");
        XSSFWorkbook cnaWorkbook = getWorkbook(templateDir+"/cna_template.xlsx");


        //insert data from the datalists to the template
        if(metadataWorkbook != null){

            updateSheetWithData(metadataWorkbook.getSheetAt(1), patientSheetData, 6, 2);
            updateSheetWithData(metadataWorkbook.getSheetAt(2), patientTumorSheetData,6, 2);
            updateSheetWithData(metadataWorkbook.getSheetAt(3), pdxModelSheetData,6, 2);
            updateSheetWithData(metadataWorkbook.getSheetAt(4), pdxModelValidationSheetData, 6, 2);
            updateSheetWithData(metadataWorkbook.getSheetAt(5), sharingAndContactSheetData, 6, 2 );
            updateSheetWithData(metadataWorkbook.getSheetAt(6), loaderRelatedDataSheetData, 6, 2);
        }

        if(samplePlatformWorkbook != null){

            updateSheetWithData(samplePlatformWorkbook.getSheetAt(0), samplePlatformDescriptionSheetData, 6, 1);
        }

        if(mutationWorkbook != null){

            updateSheetWithData(mutationWorkbook.getSheetAt(0), mutationSheetData, 2, 1);
        }

        if(cnaWorkbook != null){

            updateSheetWithData(cnaWorkbook.getSheetAt(0), cnaSheetData, 2, 1);
        }


        // Write the output to a new file
        FileOutputStream fileOut = null;
        try {

            if(metadataWorkbook != null){
                fileOut = new FileOutputStream(exportDir+"/metadata_export.xlsx");
                metadataWorkbook.write(fileOut);
                fileOut.close();
            }

            if(samplePlatformWorkbook != null){
                fileOut = new FileOutputStream(exportDir+"/sampleplatform_export.xlsx");
                samplePlatformWorkbook.write(fileOut);
                fileOut.close();
            }

            if(mutationWorkbook != null){
                fileOut = new FileOutputStream(exportDir+"/mutation_export.xlsx");
                mutationWorkbook.write(fileOut);
                fileOut.close();
            }

            if(cnaWorkbook != null){
                fileOut = new FileOutputStream(exportDir+"/cna_export.xlsx");
                cnaWorkbook.write(fileOut);
                fileOut.close();
            }


        } catch (Exception e) {
            log.error("error", e);
        }

    }

    public void initPatientData() {

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

    public void initPatientTumorAtCollection(){

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
                        log.error("Sample: {}", sampleId);
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

    public void initPdxModelDetails(){

        if (ds == null) return;

        List<ModelCreation> models = dataImportService.findModelsWithSpecimensAndQAByDS(ds.getAbbreviation());

        for(ModelCreation model : models){

            Map<String, ModelDetails> specimenMap = new HashMap<>();

            for(Specimen specimen : model.getSpecimens()){

                String passage = specimen.getPassage();

                String engraftmentSite = notSpecified;

                if(specimen.getEngraftmentSite() != null ){
                    engraftmentSite = specimen.getEngraftmentSite().getName();
                }

                String engraftmentType = notSpecified;

                if(specimen.getEngraftmentType() != null){
                   engraftmentType = specimen.getEngraftmentType().getName();
                }

                String engraftmentMaterial = notSpecified;
                String engraftmentMaterialStatus = notSpecified;

                if(specimen.getEngraftmentMaterial() != null){
                    engraftmentMaterial = specimen.getEngraftmentMaterial().getName();
                    engraftmentMaterialStatus = specimen.getEngraftmentMaterial().getState();
                }

                addEntryToSpecimenMap(specimenMap, specimen.getHostStrain(), engraftmentSite, engraftmentType, engraftmentMaterial, engraftmentMaterialStatus, passage);
            }

            insertModelSheetDataFromSpecimenMap(specimenMap, model);

        }
    }

    public void initPdxModelValidations(){

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

    public void initSharingAndContact(){

        if (ds == null) return;

        List<ModelCreation> models = dataImportService.findModelsWithSharingAndContactByDS(ds.getAbbreviation());

        for(ModelCreation model : models){

            LinkedHashMap<String, String> sharingAndContactRow = new LinkedHashMap<>();

            sharingAndContactRow.put("modelId", model.getSourcePdxId());

            Group providerGroup  = getGroupByType(model, "Provider");
            Group accessGroup = getGroupByType(model, "Accessibility");
            Group projectGroup = getGroupByType(model, "Project");

            getGroupData(sharingAndContactRow, providerGroup, accessGroup, projectGroup);



            getExternalUrlData(sharingAndContactRow, model.getExternalUrls());

            insertSharingAndContactDataForModel(sharingAndContactRow);

        }
    }

    public void initLoaderRelatedData(){

        if (ds == null) return;

        List<String> dataRow = new ArrayList<>();
        dataRow.add(ds.getName());
        dataRow.add(ds.getAbbreviation());
        dataRow.add(ds.getUrl());

        loaderRelatedDataSheetData.add(dataRow);

    }

    public void initSamplePlatformDescription(){

        if (ds == null) return;

        List<ModelCreation> models = dataImportService.findModelXenograftPlatformSampleByDS(ds.getAbbreviation());

        for(ModelCreation model : models){

            addPatientMolcharDataToSamplePlatform(model);
            addXenoMolcharDataToSamplePlatform(model);
        }

    }

    public void initMutationData(){

        initGenomicData(mutationSheetData, "mutation");

    }

    public void initCNAData(){

        initGenomicData(cnaSheetData, "copy number alteration");
    }

    private void initGenomicData(List<List<String>> sheetData, String molcharType){


        List<ModelCreation> models = dataImportService.findModelsWithSharingAndContactByDS(ds.getAbbreviation());

        for(ModelCreation m: models){

            ModelCreation model = dataImportService.findModelWithMolecularDataByDSAndIdAndMolcharType(ds.getAbbreviation(), m.getSourcePdxId(), molcharType);

            if(model != null){

                String modelId = model.getSourcePdxId();
                log.info("Exporting data for {}", modelId);

                initPatientGenomicData(model, sheetData);
                initXenoGenomicData(model, sheetData);
            }

        }

    }

    private void insertOmicDataToSheet(ModelCreation model, String sampleId, String sampleOrigin, String molcharType, Specimen specimen, MolecularCharacterization mc, List<List<String>> sheetData){

        for(MarkerAssociation ma: mc.getMarkerAssociations()){

            List<String> rowData = new ArrayList<>();

            rowData.add(model.getDataSource());
            rowData.add(model.getSourcePdxId());
            rowData.add(sampleId);
            rowData.add(sampleOrigin);

            if(sampleOrigin.equals(patientOrigin)){
                //no passage, host strain for patient samples
                rowData.add("");
                rowData.add("");
            }
            else{

                rowData.add(specimen.getPassage());

                if(specimen.getHostStrain() != null && specimen.getHostStrain().getSymbol() != null){
                    rowData.add(specimen.getHostStrain().getSymbol());
                }
                else{
                    rowData.add("");
                }
            }

            //then get the MA data inserted

            if(molcharType.equals("mutation")){
                rowData.add(ma.getMarker().getHgncSymbol());
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

                rowData.add(ma.getChromosome());
                rowData.add(ma.getSeqStartPosition());
                rowData.add(ma.getSeqEndPosition());
                rowData.add(ma.getMarker().getHgncSymbol());
                rowData.add(ma.getMarker().getUcscGeneId());
                rowData.add(ma.getMarker().getNcbiGeneId());
                rowData.add(ma.getMarker().getEnsemblGeneId());
                rowData.add(ma.getCnaLog10RCNA());
                rowData.add(ma.getCnaLog2RCNA());
                rowData.add(ma.getFold_change());
                rowData.add(ma.getCnaCopyNumberStatus());
                rowData.add(ma.getCnaGisticValue());
                rowData.add(ma.getCnaPicnicValue());
                rowData.add(ma.getGenomeAssembly());
                rowData.add(mc.getPlatform().getName());
            }

            sheetData.add(rowData);
        }
    }

    public void updateSheetWithData(Sheet sheet, List<List<String>> data, int startRow, int startColumn){

        //Workbook rows and cells start at index 0

        for(int i = 0; i < data.size(); i++){

            int rowIndex = startRow + i - 1;
            sheet.createRow(rowIndex);

            for(int j = 0; j < data.get(i).size(); j++){


                int columnIndex = startColumn + j-1;
                sheet.getRow(rowIndex).createCell(columnIndex);


                Cell cell = null;
                try{
                    cell = sheet.getRow(rowIndex).getCell(columnIndex);
                    cell.setCellValue(data.get(i).get(j));
                }
                catch (Exception e){

                    log.error("Exception in {}  {}:{}",sheet.getSheetName(), rowIndex, columnIndex);
                }
            }
        }

    }

    private String getPubmedIDs(ModelCreation model){

        StringBuilder pubmedIDs = new StringBuilder("");

        if(model.getGroups() != null){

            for(Group g : model.getGroups()){

                if(g.getType().equals("Publication")){

                    if(pubmedIDs.length() != 0){
                        pubmedIDs.append(",");
                    }

                    pubmedIDs.append(g.getPubMedId());
                }
            }
        }

        return pubmedIDs.toString();
    }

    private void insertModelSheetDataFromSpecimenMap(Map<String, ModelDetails> specimenMap, ModelCreation model){

        String modelId = model.getSourcePdxId();

        String pubmedIDs = getPubmedIDs(model);

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

    private void insertSharingAndContactDataForModel(LinkedHashMap<String, String> sharingAndContactRow){

        List<String> dataRow = new ArrayList<>();

        dataRow.add(sharingAndContactRow.get("modelId"));
        dataRow.add(sharingAndContactRow.get("providerType"));
        dataRow.add(sharingAndContactRow.get("modelAccessibility"));
        dataRow.add(sharingAndContactRow.get("accessModalities"));
        dataRow.add(sharingAndContactRow.get("contactEmail"));
        dataRow.add(sharingAndContactRow.get("contactName"));
        dataRow.add(sharingAndContactRow.get("contactLink"));
        dataRow.add(sharingAndContactRow.get("modelLink"));
        dataRow.add(sharingAndContactRow.get("providerName"));
        dataRow.add(sharingAndContactRow.get("providerAbbrev"));
        dataRow.add(sharingAndContactRow.get("projectName"));

        sharingAndContactSheetData.add(dataRow);
    }

    private Group getGroupByType(ModelCreation model, String type){

        if(model.getGroups() != null){

            for(Group g : model.getGroups()){

                if(g.getType().equals(type)){
                    return g;
                }
            }
        }

        return null;
    }

    private void getGroupData(LinkedHashMap<String, String> map, Group providerGroup, Group accessGroup, Group projectGroup){

        map.put("providerType", providerGroup.getProviderType());
        map.put("providerName",  providerGroup.getName());
        map.put("providerAbbrev", providerGroup.getAbbreviation());

        if(accessGroup != null){

            map.put("modelAccessibility", accessGroup.getAccessibility());
            map.put("accessModalities", accessGroup.getAccessModalities());
        }
        else{
            map.put("modelAccessibility", "");
            map.put("accessModalities", "");
        }

        if(projectGroup != null){

            map.put("projectName", projectGroup.getName());
        }
        else {
            map.put("projectName", "");
        }
    }

    private void getExternalUrlData(LinkedHashMap map, Collection<ExternalUrl> urls){


        map.put("contactEmail",  "");
        map.put("contactName",  "");
        map.put("contactLink",  "");
        map.put("modelLink",  "");

        for(ExternalUrl ex: urls){

            if(ex.getType().equals("contact")){

                if(ex.getUrl()!= null && ex.getUrl().contains("@")){
                    map.put("contactEmail", ex.getUrl());
                }
                else{
                    map.put("contactLink", ex.getUrl());
                }
            }

            else if(ex.getType().equals("source") && ex.getUrl() != null){

                map.put("modelLink", ex.getUrl());
            }
        }
    }

    private void addEntryToSpecimenMap(Map<String, ModelDetails> specimenMap, HostStrain hostStrain, String engraftmentSite, String engraftmentType,
                                       String engraftmentMaterial, String engraftmentMaterialStatus, String passage){

        String specimenMapKey = hostStrain.getName() + hostStrain.getSymbol() + engraftmentSite + engraftmentType + engraftmentMaterial + engraftmentMaterialStatus;


        if(specimenMap.containsKey(specimenMapKey)){

            specimenMap.get(specimenMapKey).getPassages().add(passage);
        }
        else{

            ModelDetails md = new ModelDetails(hostStrain.getName(), hostStrain.getSymbol(), engraftmentSite, engraftmentType, engraftmentMaterial, engraftmentMaterialStatus, passage);
            specimenMap.put(specimenMapKey, md);
        }
    }

    private void addPatientMolcharDataToSamplePlatform(ModelCreation model){

        //get the patient sample related molchars first
        if(model.getSample() != null && model.getSample().getMolecularCharacterizations() != null){

            for(MolecularCharacterization mc : model.getSample().getMolecularCharacterizations()){

                List<String> dataRow = new ArrayList<>();

                dataRow.add(model.getSample().getSourceSampleId());
                dataRow.add(patientOrigin);
                dataRow.add("NA");
                dataRow.add("");
                dataRow.add(model.getSourcePdxId());
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

    private void addXenoMolcharDataToSamplePlatform(ModelCreation model){

        if(model.getSpecimens() != null){

            for(Specimen sp : model.getSpecimens()){

                String passage = sp.getPassage();

                String hostStrainName = "";
                String hostStrainNomenclature = "";

                if(sp.getHostStrain() != null){

                    hostStrainName = getHostStrainName(sp);
                    hostStrainNomenclature = getHostStrainNomenclature(sp);
                }


                Sample sample = sp.getSample();

                for(MolecularCharacterization mc : sample.getMolecularCharacterizations()){

                    List<String> dataRow = new ArrayList<>();

                    dataRow.add(sample.getSourceSampleId());
                    dataRow.add("xenograft");
                    dataRow.add(passage);
                    dataRow.add("");
                    dataRow.add(model.getSourcePdxId());
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

    private void initPatientGenomicData(ModelCreation model, List<List<String>> sheetData){

        //check for molchars on patient sample
        if(model.getSample() != null && model.getSample().getMolecularCharacterizations() != null){

            String sampleId = model.getSample().getSourceSampleId();

            for(MolecularCharacterization mc : model.getSample().getMolecularCharacterizations()){

                insertOmicDataToSheet(model, sampleId, patientOrigin, mc.getType(), null, mc, sheetData);
            }
        }
    }

    private void initXenoGenomicData(ModelCreation model, List<List<String>> sheetData){

        //then look for xenograft molchars
        if(model.getSpecimens()!= null){

            for(Specimen sp : model.getSpecimens()){

                if(sp.getSample() != null && sp.getSample().getMolecularCharacterizations() != null){

                    String sampleId = sp.getSample().getSourceSampleId();

                    for(MolecularCharacterization mc: sp.getSample().getMolecularCharacterizations()){

                        insertOmicDataToSheet(model, sampleId,"xenograft", mc.getType(), sp, mc, sheetData);
                    }
                }
            }
        }
    }


    private XSSFWorkbook getWorkbook(String templatePath) {

        File file = new File(templatePath);
        log.debug("Loading template {}", templatePath);
        if (!file.exists()) {
            log.error("Template not found {}", templatePath);
            return null;
        }


        try (FileInputStream fileInputStream = new FileInputStream(file)) {

            return new XSSFWorkbook(fileInputStream);

        } catch (IOException e) {
            log.error("There was a problem accessing the file: {}", e);
        }
        return null;
    }

    private String getHostStrainName(Specimen sp){

        return sp.getHostStrain().getName() == null?"":sp.getHostStrain().getName();
    }

    private String getHostStrainNomenclature(Specimen sp){

        return sp.getHostStrain().getSymbol() == null?"":sp.getHostStrain().getSymbol();
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

    public List<List<String>> getDrugDosingSheetData() {
        return drugDosingSheetData;
    }

    public List<List<String>> getMutationSheetData() {
        return mutationSheetData;
    }

    public List<List<String>> getCnaSheetData() {
        return cnaSheetData;
    }


}
