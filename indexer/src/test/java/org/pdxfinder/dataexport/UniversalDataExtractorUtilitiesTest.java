package org.pdxfinder.dataexport;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.pdxfinder.BaseTest;
import org.pdxfinder.TSV;
import org.pdxfinder.graph.dao.*;
import org.pdxfinder.services.DataImportService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;

public class UniversalDataExtractorUtilitiesTest extends BaseTest {

    @Mock
    private DataImportService dataImportService;

    @InjectMocks
    protected UniversalDataExtractionServices extractor;

    Group providerGroup;

    private static final String ncbiId = "XR_929159.2";
    private static final String biotype = "protien_coding";
    private static final String codingSequenceChange = "3154G>A";
    private static final String variantClass = "SNV";
    private static final String cnaLog2RCNA = "-0.01234";
    private static final String marker = "SMAD3";
    private static final String patientSampleId = "s123";
    private static final String xenoSampleId = "xs123";
    private static final String mutMolType = TSV.molecular_characterisation_type.mut.mcType;
    private static final String cnaMolType = TSV.molecular_characterisation_type.cna.mcType;

    private String TEST_DRUG = "TEST_DRUG";
    private String TEST_DOSE = "TEST_DOSE";
    private String TEST_RESPONSE = "TEST_RESPONSE";
    private String MODEL_ID = "m123";
    private String PATIENT_ID = "PATIETNID";

    @Before
    public void setUp() {
        providerGroup = Group.createProviderGroup("TestGroup", "TG", "", "Academia", "Bob", "Bob's page");
    }

    @Test
    public void Given_PatientAndProvider_When_GetPatientSheetIsCalled_Then_PatientDataIsInRowOne() {
        when(dataImportService.findPatientsByGroup(providerGroup))
                .thenReturn(getPatientListForTest());

        List<List<String>> patientData  = extractor.extractPatientSheet(providerGroup);
        Assert.assertEquals("p123", patientData.get(0).get(0));
    }

    @Test
    public void Given_PatientAndModelAndSample_When_PatientSampleSheetIsCalled_Then_PatientDataIsInRowOne(){
        Patient patient = new Patient("p123", "male", "", "", providerGroup);
        PatientSnapshot patientSnapshot = new PatientSnapshot();
        patientSnapshot.setAgeAtCollection("65");
        Sample sample = new Sample();
        sample.setSourceSampleId("s123");

        TumorType tt = new TumorType("Metastatic");
        Tissue ot = new Tissue("Brain");
        Tissue ss = new Tissue("Brain");

        sample.setType(tt);
        sample.setOriginTissue(ot);
        sample.setSampleSite(ss);

        patientSnapshot.addSample(sample);
        patient.addSnapshot(patientSnapshot);

        List<Patient> patientList = new ArrayList<>();
        patientList.add(patient);

        ModelCreation modelCreation = new ModelCreation();
        modelCreation.setSourcePdxId(MODEL_ID);

        when(dataImportService.findPatientTumorAtCollectionDataByDS(providerGroup))
                .thenReturn(patientList);
        when(dataImportService.findModelBySample(sample))
                .thenReturn(modelCreation);

        extractor.extractSampleSheet(providerGroup, false);

        List<List<String>> patientSampleSheet = extractor.extractSampleSheet(providerGroup, false);
        Assert.assertEquals("p123", patientSampleSheet.get(0).get(0));
    }

    @Test
    public void Given_ModelWithDetails_When_GetModelDetailsIsCalled_Then_ModelDataIsInRowOne(){

        when( dataImportService.findModelsWithSpecimensAndQAByDS(providerGroup.getAbbreviation()))
                .thenReturn(getModelListForTest());

        extractor.extractModelDetails(providerGroup);
        extractor.extractModelValidations(providerGroup);

        List<List<String>> pdxModelDetails = extractor.extractModelDetails(providerGroup);
        List<List<String>> pdxModelValidations = extractor.extractModelValidations(providerGroup);

        Assert.assertEquals(MODEL_ID, pdxModelDetails.get(0).get(0));
        Assert.assertEquals("hsname", pdxModelDetails.get(0).get(1));

        Assert.assertEquals(MODEL_ID, pdxModelValidations.get(0).get(0));
        Assert.assertEquals("technology", pdxModelValidations.get(0).get(1));

    }

    @Test
    public void Given_Provider_When_GetSharingAndContactSheetIsCalled_Then_SharingAndContactDataIsInRowOne(){
        when(dataImportService.findModelsWithSharingAndContactByDS(providerGroup.getAbbreviation()))
                .thenReturn(getModelListForTest());

        List<List<String>> sharingAndContact = extractor.extractSharingAndContact(providerGroup);

        Assert.assertEquals(MODEL_ID, sharingAndContact.get(0).get(0));
        Assert.assertEquals("Academia", sharingAndContact.get(0).get(1));
    }

    @Test
    public void Given_Provider_When_GetLoaderRelatedSheetIsCalled_Then_DataIsInRowOne(){
        List<List<String>> loaderRelatedData = extractor.extractLoaderRelatedData(providerGroup);
        Assert.assertEquals("TG", loaderRelatedData.get(0).get(1));
    }

    @Test
    public void Given_ModelWithMolecularData_When_GetSamplePlatformSheetIsCalled_Then_SamplePlatformDataIsInRowOne(){

        when(dataImportService.findModelXenograftPlatformSampleByDS(providerGroup.getAbbreviation()))
                .thenReturn(getModelListForTest());

        List<List<String>> samplePlatformDescription = extractor.extractSamplePlatform(providerGroup);

        Assert.assertEquals(patientSampleId, samplePlatformDescription.get(0).get(1));
        Assert.assertEquals(xenoSampleId, samplePlatformDescription.get(2).get(1));
    }

    @Test
    public void Given_XenograftSamplewithMutationAndCna_When_extractGroupOmicDataIsCalled_Then_returnAppropriateDataForEach(){
        List<ModelCreation> modelList = getModelListForTest();
        ModelCreation testModel = modelList.get(0);

        List<List<String>> mutationData = extractor.extractModelsOmicData(modelList.get(0),mutMolType);
        List<List<String>> cnaData = extractor.extractModelsOmicData(testModel,cnaMolType);
        Assert.assertTrue(mutationData.get(0).contains(ncbiId));
        Assert.assertTrue(mutationData.get(0).contains(biotype));
        Assert.assertTrue(mutationData.get(0).contains(codingSequenceChange));
        Assert.assertTrue(mutationData.get(0).contains(variantClass));
        Assert.assertFalse(mutationData.get(0).contains(cnaLog2RCNA));
        Assert.assertFalse(mutationData.get(0).contains(marker));

        Assert.assertTrue(cnaData.get(0).contains(cnaLog2RCNA));
        Assert.assertTrue(cnaData.get(0).contains(marker));
        Assert.assertFalse(cnaData.get(0).contains(biotype));
        Assert.assertFalse(cnaData.get(0).contains(variantClass));
    }

    @Test
    public void Given_PatientSamplewithMutationAndCna_When_extractGroupOmicDataIsCalled_Then_returnAppropriateDataForEach(){
        List<ModelCreation> modelList = getModelListForTest();
        ModelCreation testModel = modelList.get(0);
        List<List<String>> mutationData = extractor.extractModelsOmicData(testModel, mutMolType);
        List<List<String>> cnaData = extractor.extractModelsOmicData(testModel, cnaMolType);
        Assert.assertTrue(mutationData.get(0).contains(ncbiId));
        Assert.assertFalse(mutationData.get(0).contains(marker));
        Assert.assertTrue(cnaData.get(0).contains(marker));
        Assert.assertFalse(cnaData.get(0).contains(biotype));
    }


    @Test
    public void Given_ModelwithMutationData_When_extractModelDataIsCalledwithDifferentMolcType_Then_NoDataIsRetrieved(){
        List<ModelCreation> modelList = getModelListForTest();
        ModelCreation testModel = modelList.get(0);
        List<List<String>> expressionData = extractor.extractModelsOmicData(testModel,"expression");
        Assert.assertEquals(0, expressionData.size());
    }

    @Test
    public void Given_ModelwithDosingData_When_extractModelIsCalled_Then_DrugDosingIsRetrieved(){
        List<ModelCreation> modelList = getModelListForTest();
        ModelCreation testModel = modelList.get(0);
        testModel.setTreatmentSummary(buildTreatmentSummaryTreeToModel());
        List<List<String>> dosingData = extractor.extractModelsOmicData(testModel,"drug");
        Assert.assertEquals(1, dosingData.size());
        Assert.assertEquals(MODEL_ID, dosingData.get(0).get(2));
        Assert.assertEquals(TEST_DRUG, dosingData.get(0).get(4));
        Assert.assertEquals(TEST_DOSE, dosingData.get(0).get(6));
        Assert.assertEquals(TEST_RESPONSE, dosingData.get(0).get(10));
    }

    @Test
    public void Given_ModelwithPatientData_When_extractModelIsCalled_Then_PatientTreatmentIsRetrieved(){
        List<ModelCreation> modelList = getModelListForTest();
        ModelCreation testModel = modelList.get(0);
        testModel.getSample().getPatientSnapshot().setTreatmentSummary(buildTreatmentSummaryTreeToModel());
        List<List<String>> dosingData = extractor.extractModelsOmicData(testModel,"patientTreatment");
        Assert.assertEquals(1, dosingData.size());
        Assert.assertEquals(PATIENT_ID, dosingData.get(0).get(0));
        Assert.assertEquals(TEST_DRUG, dosingData.get(0).get(1));
        Assert.assertEquals(TEST_DOSE, dosingData.get(0).get(2));
        Assert.assertEquals(TEST_RESPONSE, dosingData.get(0).get(7));
    }



    private List<ModelCreation> getModelListForTest(){
        List<ModelCreation> modelCreationList = new ArrayList<>();

        ModelCreation model = new ModelCreation();
        model.setSourcePdxId(MODEL_ID);

        setModelGroups(model);
        setExternalUrl(model);

        QualityAssurance qualityAssurance = new QualityAssurance("technology", "description", "1,2");
        model.addQualityAssurance(qualityAssurance);

        Patient patient = new Patient(PATIENT_ID, new Group());
        PatientSnapshot patientSnapshot = new PatientSnapshot(patient, "0");

        Sample patientSample = new Sample();
        patientSample.setSourceSampleId(patientSampleId);
        patientSample.setPatientSnapshot(patientSnapshot);

        model.setSample(patientSample);

        Specimen specimen = new Specimen();
        specimen.setPassage("1");
        specimen.setHostStrain(new HostStrain("hssymbol", "hsname"));

        Sample xenoSample = new Sample();
        xenoSample.setSourceSampleId(xenoSampleId);
        specimen.setSample(xenoSample);
        model.addSpecimen(specimen);

        String mutJson = "[{\"biotype\": \"" + biotype + "\",\"codingSequenceChange\":\"" + codingSequenceChange + "\"," +
                "\"variantClass\":\""+ variantClass + "\",\"codonChange\"" +
                ":\"Gtt/Att\",\"aminoAcidChange\":\"E763*\",\"consequence\":\"\",\"functionalPrediction\":\"Nonsense_Mutation\"," +
                "\"readDepth\":\"403\",\"alleleFrequency\":\"0.464\",\"chromosome\":\"5\",\"seqStartPosition\":\"112173578\"," +
                "\"refAllele\":\"G\",\"altAllele\":\"T\",\"ucscGeneId\":\"\",\"ncbiGeneId\":\"" + ncbiId + "\",\"ncbiTranscriptId\":" +
                "\"XR_929159.2\",\"existingVariations\":\"CM106354,COSM5010432\",\"genomeAssembly\":\"hg19\",\"nucleotideChange\"" +
                ":\"\",\"marker\":\"APC\"}]";

        String cnaJson = "[{\"chromosome\":\"\",\"seqStartPosition\":\"\",\"genomeAssembly\":\"\"," +
                "\"seqEndPosition\":\"\",\"cnaLog10RCNA\":\"\",\"cnaLog2RCNA\":\"" + cnaLog2RCNA + "\"," +
                "\"cnaCopyNumberStatus\":\"Normal\",\"cnaGisticValue\":\"\"" +
                ",\"cnaPicnicValue\":\"\",\"marker\":\"" + marker + "\"}]";

        Platform platform = new Platform();
        platform.setName("platform");
        platform.setUrl("platformurl");

        MolecularCharacterization mutMc = createMolecularDataStructure(mutMolType, mutJson, platform);
        MolecularCharacterization cnaMc = createMolecularDataStructure(cnaMolType, cnaJson, platform);
        xenoSample.addMolecularCharacterization(mutMc);
        xenoSample.addMolecularCharacterization(cnaMc);
        patientSample.addMolecularCharacterization(mutMc);
        patientSample.addMolecularCharacterization(cnaMc);

        modelCreationList.add(model);
        return modelCreationList;
    }

    private void setModelGroups(ModelCreation model){
        Group accessGroup = Group.createAccessibilityGroup("Academia", "transnational");
        Group project = new Group("project1", "p1", "Project");

        Group publicationGroup = new Group();
        publicationGroup.setType("Publication");
        publicationGroup.setPubMedId("12345");

        model.addGroup(publicationGroup);
        model.addGroup(providerGroup);
        model.addGroup(accessGroup);
        model.addGroup(project);
    }

    private void setExternalUrl(ModelCreation model){
        ExternalUrl url = new ExternalUrl(ExternalUrl.Type.CONTACT,"email@address.com");
        List<ExternalUrl> urlList = new ArrayList<>();
        urlList.add(url);
        model.setExternalUrls(urlList);
    }

    private List<Patient> getPatientListForTest(){

        Patient patient = new Patient("p123", "male", "", "", providerGroup);
        patient.setCancerRelevantHistory("");
        patient.setFirstDiagnosis("");
        patient.setAgeAtFirstDiagnosis("60");

        List<Patient> patientList = new ArrayList<>();
        patientList.add(patient);

        return patientList;
    }

    private MolecularCharacterization createMolecularDataStructure(String molType, String markerAssociation, Platform platform){
        MolecularCharacterization molecularCharacterization = new MolecularCharacterization();
        molecularCharacterization.setType(molType);
        molecularCharacterization.setTechnology("techtest");

        MarkerAssociation ma = new MarkerAssociation();
        ma.setMolecularDataString(markerAssociation);

        molecularCharacterization.setMarkerAssociations(Collections.singletonList(ma));
        molecularCharacterization.setPlatform(platform);
        return molecularCharacterization;
    }

    private TreatmentSummary buildTreatmentSummaryTreeToModel(){
        Treatment treatment = new Treatment(TEST_DRUG);
        TreatmentComponent treatmentComponent = new TreatmentComponent(TEST_DOSE, treatment);
        TreatmentProtocol treatmentProtocol = new TreatmentProtocol();
        treatmentProtocol.setComponents(Collections.singletonList(treatmentComponent));
        treatmentProtocol.setResponse(new Response(TEST_RESPONSE, treatmentProtocol));
        TreatmentSummary treatmentSummary = new TreatmentSummary();
        treatmentSummary.setTreatmentProtocols(Collections.singletonList(treatmentProtocol));
        return treatmentSummary;
    }
}