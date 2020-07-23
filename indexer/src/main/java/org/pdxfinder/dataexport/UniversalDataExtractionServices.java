package org.pdxfinder.dataexport;

import org.pdxfinder.TSV;
import org.pdxfinder.graph.dao.*;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/*
 * Created by csaba on 02/10/2019.
 */
@Service
public class UniversalDataExtractionServices {

    private static final Logger log = LoggerFactory.getLogger(UniversalDataExtractionServices.class);

    private UtilityService utilityService;
    private DataImportService dataImportService;

    public static final String MODEL_ID = "modelId";
    public static final String PROVIDER_TYPE = "providerType";
    public static final String MODEL_ACCESSIBILITY = "modelAccessibility";
    public static final String ACCESS_MODALITIES = "accessModalities";
    public static final String CONTACT_EMAIL = "contactEmail";
    public static final String CONTACT_NAME = "contactName";
    public static final String CONTACT_LINK = "contactLink";
    public static final String MODEL_LINK = "modelLink";
    public static final String PROVIDER_NAME = "providerName";
    public static final String PROVIDER_ABBREV = "providerAbbrev";
    public static final String PROJECT_NAME = "projectName";
    private static final String NOT_SPECIFIED = "Not Specified";
    private static final String PATIENT_ORIGIN = "patient";
    private static final String MODEL_ORIGIN = "xenograft";

    @Autowired
    public UniversalDataExtractionServices(DataImportService dataImportService, UtilityService utilityService) {
        this.dataImportService = dataImportService;
        this.utilityService = utilityService;
    }

    public MetadataSheets extractMetadata(Group group, MetadataSheets sheets, boolean isHarmonized){
        sheets.set(TSV.metadataSheetNames.checklist.name(), new ArrayList<>());
        sheets.set(TSV.metadataSheetNames.patient.name(), extractPatientSheet(group));
        sheets.set(TSV.metadataSheetNames.sample.name(), extractSampleSheet(group, isHarmonized));
        sheets.set(TSV.metadataSheetNames.model.name(), extractModelDetails(group));
        sheets.set(TSV.metadataSheetNames.model_validation.name(), extractModelValidations(group));
        sheets.set(TSV.metadataSheetNames.sharing.name(), extractSharingAndContact(group));
        sheets.set(TSV.metadataSheetNames.loader.name(), extractLoaderRelatedData(group));
        return sheets;
    }

    public List<List<String>> extractSamplePlatform(Group group){
        return extractSamplePlatformDescription(group);
    }

    public List<ModelCreation> getAllModelsByGroupAndMoleculartype(Group group, String molcharType){
        return dataImportService.findModelWithMolecularDataByDSAndIdAndMolcharType(
                        group.getAbbreviation(),
                        molcharType);
        }


    public List<List<String>> extractPatientSheet(Group group) {
        List<Patient> patients = dataImportService.findPatientsByGroup(group);
        List<List<String>> patientSheetDataExport = new ArrayList<>();
        for (Patient patient : patients) {

            List<String> dataRow = new ArrayList<>();

            String patientId = patient.getExternalId();
            String sex = patient.getSex();
            String cancerHistory = patient.getCancerRelevantHistory();
            String ethnicity = patient.getEthnicity();
            String ethnicityAssessment = patient.getEthnicityAssessment();
            String firstDiagnosis = patient.getFirstDiagnosis();
            String ageAtFirstDiagnosis = patient.getAgeAtFirstDiagnosis();

            dataRow.add(patientId);
            dataRow.add(sex);
            dataRow.add(cancerHistory);
            dataRow.add(ethnicity);
            dataRow.add(ethnicityAssessment);
            dataRow.add(firstDiagnosis);
            dataRow.add(ageAtFirstDiagnosis);

            patientSheetDataExport.add(dataRow);
        }
        return patientSheetDataExport;
    }

    public List<List<String>> extractSampleSheet(Group group, boolean isHarmonized){
        List<Patient> patients = dataImportService.findPatientTumorAtCollectionDataByDS(group);
        List<List<String>> sampleSheetDataExport = new ArrayList<>();
        for(Patient patient : patients){
            String patientId = patient.getExternalId();
            for(PatientSnapshot patientSnapshot : patient.getSnapshots()){
                for(Sample sample : patientSnapshot.getSamples()){

                    List<String> dataRow = new ArrayList<>();
                    String sampleId = sample.getSourceSampleId();
                    String collectionDate = patientSnapshot.getDateAtCollection();
                    String collectionEvent = patientSnapshot.getCollectionEvent();
                    String elapsedTime = patientSnapshot.getElapsedTime();
                    String ageAtCollection = patientSnapshot.getAgeAtCollection();
                    String diagnosis = getSampleUnharmonizedDiagnosis(sample);
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
                    String modelId = getModelIdBySample(sample);

                    dataRow.add(patientId);
                    dataRow.add(sampleId);
                    dataRow.add(collectionDate);
                    dataRow.add(collectionEvent);
                    dataRow.add(elapsedTime);
                    dataRow.add(ageAtCollection);
                    dataRow.add(diagnosis);
                    addHarmonizedDiagnosis(dataRow, sample, isHarmonized);
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
                    sampleSheetDataExport.add(dataRow);
                }
            }
        }
        return sampleSheetDataExport;
    }

    private void addHarmonizedDiagnosis(List<String> dataRow, Sample sample, boolean isHarmonized){
        if(isHarmonized){
            String harmonizedDiagnosis = getHarmonizedDiagnosis(sample);
            dataRow.add(harmonizedDiagnosis);
        }
    }

    private String getHarmonizedDiagnosis(Sample sample){
        String diagnosis = "";
        try{
            diagnosis = sample.getSampleToOntologyRelationship().getOntologyTerm().getLabel();
        } catch (NullPointerException e){
            log.warn("Sample {} was not mapped to an ontology term", sample.getSourceSampleId());
        }
        return diagnosis;
    }

    private String getSampleUnharmonizedDiagnosis(Sample sample) {
        return sample.getDiagnosis();
    }

    private String getModelIdBySample(Sample sample){
        String modelId = "";
        try {
            modelId = dataImportService.findModelBySample(sample).getSourcePdxId();
        }
        catch (Exception e){
            log.error("Could not find Model associated with Sample: {} ", sample.getSourceSampleId());
        }
        return modelId;
    }

    public List<List<String>> extractModelDetails(Group group){

        List<ModelCreation> models = dataImportService.findModelsWithSpecimensAndQAByDS(group.getAbbreviation());
        List<List<String>> modelSheetDataExport = new ArrayList<>();
        for(ModelCreation model : models){
            Map<String, ModelDetails> specimenMap = new HashMap<>();
            for(Specimen specimen : model.getSpecimens()){

                String passage = specimen.getPassage();
                String engraftmentSite = getEngraftmentSite(specimen);
                String engraftmentType = getEngraftmentType(specimen);
                String[] engraftmentMaterialInfo = getEngraftmentMaterialInfo(specimen);
                String engraftmentMaterial = engraftmentMaterialInfo[0];
                String engraftmentMaterialStatus = engraftmentMaterialInfo[1];

                addEntryToSpecimenMap(
                    specimenMap,
                    specimen.getHostStrain(),
                    engraftmentSite,
                    engraftmentType,
                    engraftmentMaterial,
                    engraftmentMaterialStatus,
                    passage);
            }
            modelSheetDataExport.addAll(insertModelSheetDataFromSpecimenMap(specimenMap, model));
        }
        return modelSheetDataExport;
    }

    private List<List<String>> insertModelSheetDataFromSpecimenMap(Map<String, ModelDetails> specimenMap, ModelCreation model){
        List<List<String>> modelSheetDataExport = new ArrayList<>();
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

            modelSheetDataExport.add(dataRow);
        }
        return modelSheetDataExport;
    }

    private String[] getEngraftmentMaterialInfo(Specimen specimen){
        String[] engraftmentInfo = {NOT_SPECIFIED, NOT_SPECIFIED};
        if(Objects.nonNull(specimen.getEngraftmentMaterial())){
          engraftmentInfo[0] =  specimen.getEngraftmentMaterial().getName();
          engraftmentInfo[1] = specimen.getEngraftmentMaterial().getState();
        }
        return engraftmentInfo;
    }

    private String getEngraftmentSite(Specimen specimen){
        String engraftmentSite = NOT_SPECIFIED;
        if (Objects.nonNull(specimen.getEngraftmentSite())){
            engraftmentSite = specimen.getEngraftmentSite().getName();
        }
        return engraftmentSite;
    }

    private String getEngraftmentType(Specimen specimen){
        String engraftmentType = NOT_SPECIFIED;
        if (Objects.nonNull(specimen.getEngraftmentType())){
            engraftmentType = specimen.getEngraftmentType().getName();
        }
        return engraftmentType;
    }

    public List<List<String>> extractModelValidations(Group group){
        List<List<String>>  modelValidationSheetDataExport = new ArrayList<>();
        List<ModelCreation> models = dataImportService.findModelsWithSpecimensAndQAByDS(group.getAbbreviation());

        for(ModelCreation model : models){

            if(model != null && model.getQualityAssurance() != null) {

                String modelId = model.getSourcePdxId();

                for (QualityAssurance qa : model.getQualityAssurance()) {

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
                    modelValidationSheetDataExport.add(dataRow);
                }
            }
        }
        return modelValidationSheetDataExport;
    }

    public List<List<String>> extractSharingAndContact(Group group){
        List<ModelCreation> models = dataImportService.findModelsWithSharingAndContactByDS(group.getAbbreviation());
        List<List<String>> sharingAndContactSheetDataExport = new ArrayList<>();

        for(ModelCreation model : models){

            LinkedHashMap<String, String> sharingAndContactRow = new LinkedHashMap<>();
            sharingAndContactRow.put(MODEL_ID, model.getSourcePdxId());
            Group providerGroup = getGroupByType(model, "Provider");
            Group accessGroup = getGroupByType(model, "Accessibility");
            Group projectGroup = getGroupByType(model, "Project");
            getGroupData(sharingAndContactRow, providerGroup, accessGroup, projectGroup);
            getExternalUrlData(sharingAndContactRow, model.getExternalUrls());

            List<String> dataRow = new ArrayList<>();
            dataRow.add(sharingAndContactRow.get(MODEL_ID));
            dataRow.add(sharingAndContactRow.get(PROVIDER_TYPE));
            dataRow.add(sharingAndContactRow.get(MODEL_ACCESSIBILITY));
            dataRow.add(sharingAndContactRow.get(ACCESS_MODALITIES));
            dataRow.add(sharingAndContactRow.get(CONTACT_EMAIL));
            dataRow.add(sharingAndContactRow.get(CONTACT_NAME));
            dataRow.add(sharingAndContactRow.get(CONTACT_LINK));
            dataRow.add(sharingAndContactRow.get(MODEL_LINK));
            dataRow.add(sharingAndContactRow.get(PROVIDER_NAME));
            dataRow.add(sharingAndContactRow.get(PROVIDER_ABBREV));
            dataRow.add(sharingAndContactRow.get(PROJECT_NAME));
            sharingAndContactSheetDataExport.add(dataRow);
        }
        return sharingAndContactSheetDataExport;
    }

    public List<List<String>> extractLoaderRelatedData(Group group){
        List<List<String>> loaderRelatedDataSheetDataExport = new ArrayList<>();
        List<String> dataRow = new ArrayList<>();
        dataRow.add(group.getName());
        dataRow.add(group.getAbbreviation());
        dataRow.add(group.getUrl());
        loaderRelatedDataSheetDataExport.add(dataRow);
        return loaderRelatedDataSheetDataExport;
    }

    public List<List<String>> extractSamplePlatformDescription(Group group){
        List<List<String>> samplePlatformDescriptionSheetDataExport = new ArrayList<>();
        List<ModelCreation> models = dataImportService.findModelXenograftPlatformSampleByDS(group.getAbbreviation());
        for(ModelCreation model : models){
            samplePlatformDescriptionSheetDataExport.addAll(addPatientMolcharDataToSamplePlatform(model));
            samplePlatformDescriptionSheetDataExport.addAll(addXenoMolcharDataToSamplePlatform(model));
        }
        return samplePlatformDescriptionSheetDataExport;
    }

    public List<List<String>> extractModelsOmicData(ModelCreation model, String molcharType){
        List<List<String>> modelsOmicExportSheet = new ArrayList<>();
        if(model != null) {
            String modelId = model.getSourcePdxId();
            log.info("Exporting {} data for {}", molcharType, modelId);
            modelsOmicExportSheet.addAll(extractPatientSampleOmicData(model, molcharType));
            modelsOmicExportSheet.addAll(extractXenoSampleOmicDataForEachSpecimen(model, molcharType));
        }
        return modelsOmicExportSheet;
    }

    private List<List<String>> parseOmicDataToSheet(
        ModelCreation model,
        String sampleId,
        String sampleOrigin,
        String molcharType,
        Specimen specimen,
        MolecularCharacterization mc){

        List<List<String>> sheetData = new ArrayList<>();
        for(MarkerAssociation ma: mc.getMarkerAssociations()) {
            List<MolecularData> molecularData;
            try {
                molecularData = ma.decodeMolecularData();
            } catch (Exception e) {
                log.error("Error decoding molecular Data on sample Id {} " +
                        "for molecularCharacterization type {}", sampleId, mc.getType());
                molecularData = new ArrayList<>();
            }
            for (MolecularData md : molecularData) {
                List<String> rowData = new ArrayList<>();
                rowData.add(model.getSourcePdxId());
                rowData.add(sampleId);
                rowData.add(sampleOrigin);
                rowData.add(getHostStrainNameSymbol(specimen));
                rowData.add(getPassage(sampleOrigin, specimen));
                switch (molcharType) {
                    case "mutation":
                        rowData.add(md.getMarker());
                        rowData.add(md.getBiotype());
                        rowData.add(md.getCodingSequenceChange());
                        rowData.add(md.getVariantClass());
                        rowData.add(md.getCodonChange());
                        rowData.add(md.getAminoAcidChange());
                        rowData.add(md.getConsequence());
                        rowData.add(md.getFunctionalPrediction());
                        rowData.add(md.getReadDepth());
                        rowData.add(md.getAlleleFrequency());
                        rowData.add(md.getChromosome());
                        rowData.add(md.getSeqStartPosition());
                        rowData.add(md.getRefAllele());
                        rowData.add(md.getAltAllele());
                        rowData.add(md.getUcscGeneId());
                        rowData.add(md.getNcbiGeneId());
                        rowData.add(md.getNcbiTranscriptId());
                        rowData.add(md.getEnsemblGeneId());
                        rowData.add(md.getEnsemblTranscriptId());
                        rowData.add(md.getExistingVariations());
                        rowData.add(md.getGenomeAssembly());
                        rowData.add(mc.getPlatform().getName());
                        break;
                    case "copy number alteration":
                        rowData.add(md.getChromosome());
                        rowData.add(md.getSeqStartPosition());
                        rowData.add(md.getSeqEndPosition());
                        rowData.add(md.getMarker());
                        rowData.add(md.getUcscGeneId());
                        rowData.add(md.getNcbiGeneId());
                        rowData.add(md.getEnsemblGeneId());
                        rowData.add(md.getCnaLog10RCNA());
                        rowData.add(md.getCnaLog2RCNA());
                        rowData.add(md.getFold_change());
                        rowData.add(md.getCnaCopyNumberStatus());
                        rowData.add(md.getCnaGisticValue());
                        rowData.add(md.getCnaPicnicValue());
                        rowData.add(md.getGenomeAssembly());
                        rowData.add(mc.getPlatform().getName());
                        break;
                    case "cytogenetics":
                        rowData.add("");
                        rowData.add(md.getMarker());
                        rowData.add(md.getCytogeneticsResult());
                        rowData.add(md.getMarkerStatusComment());
                        rowData.add(mc.getPlatform().getName());
                        rowData.add("");
                        rowData.add("");
                        break;
                    case "expression":
                        rowData.add(md.getChromosome());
                        rowData.add("");
                        rowData.add(md.getSeqStartPosition());
                        rowData.add(md.getSeqEndPosition());
                        rowData.add(md.getMarker());
                        rowData.add("");
                        rowData.add("");
                        rowData.add("");
                        rowData.add(md.getRnaSeqCoverage());
                        rowData.add(md.getRnaSeqFPKM());
                        rowData.add(md.getRnaSeqTPM());
                        rowData.add(md.getRnaSeqCount());
                        rowData.add(md.getAffyHGEAProbeId());
                        rowData.add(md.getAffyHGEAExpressionValue());
                        rowData.add(md.getIlluminaHGEAProbeId());
                        rowData.add(md.getIlluminaHGEAExpressionValue());
                        rowData.add(md.getZscore());
                        rowData.add(md.getGenomeAssembly());
                        rowData.add(mc.getPlatform().getName());
                        break;
                    default:
                        throw new IllegalArgumentException("Inappropriate molecular data type passed");
                }
                sheetData.add(rowData);
            }
        }
        return sheetData;
    }

    private String getHostStrainNameSymbol(Specimen specimen) {
        String hostStrainSymbol = "";
        if (specimen != null && specimen.getHostStrain() != null && specimen.getHostStrain().getSymbol() != null) {
            hostStrainSymbol = specimen.getHostStrain().getSymbol();
           }
        return hostStrainSymbol;
    }

    private String getPassage(String sampleOrigin, Specimen specimen) {
        return (!sampleOrigin.equals(PATIENT_ORIGIN) && specimen != null) ? specimen.getPassage() : "" ;
    }

    private String getPubmedIDs(ModelCreation model){
        StringBuilder pubmedIDs = new StringBuilder();
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

        if(providerGroup != null) {
            map.put(PROVIDER_TYPE, providerGroup.getProviderType());
            map.put(PROVIDER_NAME, providerGroup.getName());
            map.put(PROVIDER_ABBREV, providerGroup.getAbbreviation());
        }
        else{
            map.put(PROVIDER_TYPE, "");
            map.put(PROVIDER_NAME, "");
            map.put(PROVIDER_ABBREV, "");
        }
        if(accessGroup != null){
            map.put(MODEL_ACCESSIBILITY, accessGroup.getAccessibility());
            map.put(ACCESS_MODALITIES, accessGroup.getAccessModalities());
        }
        else{
            map.put(MODEL_ACCESSIBILITY, "");
            map.put(ACCESS_MODALITIES, "");
        }

        if(projectGroup != null){
            map.put(PROJECT_NAME, projectGroup.getName());
        }
        else {
            map.put(PROJECT_NAME, "");
        }
    }

    private void getExternalUrlData(LinkedHashMap<String, String> map, Collection<ExternalUrl> urls){
        map.put(CONTACT_EMAIL,  "");
        map.put(CONTACT_NAME,  "");
        map.put(CONTACT_LINK,  "");
        map.put(MODEL_LINK,  "");

        if(urls != null){
            for(ExternalUrl ex: urls) {
                if (ex.getType().equals("contact")) {

                    if (ex.getUrl() != null && ex.getUrl().contains("@")) {
                        map.put(CONTACT_EMAIL, ex.getUrl());
                    } else {
                        map.put(CONTACT_LINK, ex.getUrl());
                    }
                } else if (ex.getType().equals("source") && ex.getUrl() != null) {
                    map.put(MODEL_LINK, ex.getUrl());
                }
            }
        }
    }

    private void addEntryToSpecimenMap(
        Map<String, ModelDetails> specimenMap,
        HostStrain hostStrain,
        String engraftmentSite,
        String engraftmentType,
        String engraftmentMaterial,
        String engraftmentMaterialStatus,
        String passage){

        String specimenMapKey = String.join(
            hostStrain.getName(),
            hostStrain.getSymbol(),
            engraftmentSite,
            engraftmentType,
            engraftmentMaterial,
            engraftmentMaterialStatus);

        if(specimenMap.containsKey(specimenMapKey)){
            specimenMap.get(specimenMapKey).getPassages().add(passage);
        }
        else{
            ModelDetails md = new ModelDetails(
                hostStrain.getName(),
                hostStrain.getSymbol(),
                engraftmentSite,
                engraftmentType,
                engraftmentMaterial,
                engraftmentMaterialStatus,
                passage);
            specimenMap.put(specimenMapKey, md);
        }
    }

    private ArrayList<List<String>> addPatientMolcharDataToSamplePlatform(ModelCreation model){
        ArrayList<List<String>> samplePlatformPatientSheetDataExport = new ArrayList<>();
        if(model.getSample() != null && model.getSample().getMolecularCharacterizations() != null){
            for(MolecularCharacterization mc : model.getSample().getMolecularCharacterizations()){
                List<String> dataRow = new ArrayList<>();
                dataRow.add("");
                dataRow.add(model.getSample().getSourceSampleId());
                dataRow.add(PATIENT_ORIGIN);
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

                samplePlatformPatientSheetDataExport.add(dataRow);
            }
        }
        return samplePlatformPatientSheetDataExport;
    }

    private List<List<String>> addXenoMolcharDataToSamplePlatform(ModelCreation model){
    ArrayList<List<String>> samplePlatformXenoDataSheetDataExport = new ArrayList<>();
        if(Objects.nonNull(model.getSpecimens())){
            for(Specimen sp : model.getSpecimens()){

                String passage = sp.getPassage();
                String hostStrainName = getHostStrainName(sp);
                String hostStrainNomenclature = getHostStrainNomenclature(sp);
                Sample sample = sp.getSample();

                if(sample != null && sample.getMolecularCharacterizations() != null && !sample.getMolecularCharacterizations().isEmpty()){

                    for(MolecularCharacterization mc : sample.getMolecularCharacterizations()) {

                        List<String> dataRow = new ArrayList<>();

                        dataRow.add("");
                        dataRow.add(sample.getSourceSampleId());
                        dataRow.add(MODEL_ORIGIN);
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

                        samplePlatformXenoDataSheetDataExport.add(dataRow);
                    }
                }
            }
        }
        return samplePlatformXenoDataSheetDataExport;
    }

    private List<List<String>> extractPatientSampleOmicData(ModelCreation model, String molcharType){
        List<List<String>> patientOmic = new ArrayList<>();
        if(model.getSample() != null && model.getSample().getMolecularCharacterizations() != null){
            String sampleId = model.getSample().getSourceSampleId();

            for(MolecularCharacterization mc : model.getSample().getMolecularCharacterizations()){
                if(mc.getType().equals(molcharType)){
                    patientOmic.addAll(parseOmicDataToSheet(model, sampleId, PATIENT_ORIGIN, mc.getType(), null, mc));
                }
            }
        }
        return patientOmic;
    }

    private List<List<String>> extractXenoSampleOmicDataForEachSpecimen(ModelCreation model, String molcharType) {
        List<List<String>> xenograftOmic = new ArrayList<>();
        if(model.getSpecimens() != null) {
            for (Specimen specimen : model.getSpecimens()) {
                if (specimenHasMolecularCharacterizations(specimen)) {
                    xenograftOmic.addAll(
                            extractXenoSampleOmicDataForEachMolecularCharacterization(specimen, molcharType, model));
                }
            }
        }
        if(xenograftOmic.isEmpty()){
            log.error("Problem extracting omic data for {}", model.getSourcePdxId());
        }
        return xenograftOmic;
    }

    private List<List<String>> extractXenoSampleOmicDataForEachMolecularCharacterization(Specimen specimen, String molcharType, ModelCreation model){
        List<List<String>> OmicData = new ArrayList<>();
        for (MolecularCharacterization mc : specimen.getSample().getMolecularCharacterizations()) {
            if (mc.getType().equals(molcharType)) {
                OmicData.addAll(parseOmicDataToSheet(model, specimen.getSample().getSourceSampleId(),
                        MODEL_ORIGIN, mc.getType(), specimen, mc));
            }
        }
        return OmicData;
    }

    private boolean specimenHasMolecularCharacterizations(Specimen specimen){
       return (specimen.getSample() != null && specimen.getSample().getMolecularCharacterizations() != null);
    }

    private String getHostStrainName(Specimen sp){
        return sp.getHostStrain().getName() == null? ""  :sp.getHostStrain().getName();
    }

    private String getHostStrainNomenclature(Specimen sp){
        return sp.getHostStrain().getSymbol() == null? "" :sp.getHostStrain().getSymbol();
    }
}
