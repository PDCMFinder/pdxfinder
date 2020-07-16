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

public class DataExtractorTests extends BaseTest {

    @Mock
    private DataImportService dataImportService;

    @InjectMocks
    protected UniversalDataExtractionUtilities extractor;

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

    @Before
    public void setUp() {
        providerGroup = Group.createProviderGroup("TestGroup", "TG", "", "Academia", "Bob", "Bob's page");
        extractor.init(providerGroup);
    }

    @Test
    public void Given_PatientAndProvider_When_GetPatientSheetIsCalled_Then_PatientDataIsInRowOne() {
        when(dataImportService.findPatientsByGroup(providerGroup))
          .thenReturn(getPatientListForTest());

        List<List<String>> patientData  = extractor.extractPatientSheet();
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
        modelCreation.setSourcePdxId("m123");

        when(dataImportService.findPatientTumorAtCollectionDataByDS(providerGroup))
          .thenReturn(patientList);
        when(dataImportService.findModelBySample(sample))
          .thenReturn(modelCreation);

        extractor.extractSampleSheet();

        List<List<String>> patientSampleSheet = extractor.extractSampleSheet();
        Assert.assertEquals("p123", patientSampleSheet.get(0).get(0));
    }

    @Test
    public void Given_ModelWithDetails_When_GetModelDetailsIsCalled_Then_ModelDataIsInRowOne(){

        when( dataImportService.findModelsWithSpecimensAndQAByDS(providerGroup.getAbbreviation()))
          .thenReturn(getModelListForTest());

        extractor.extractModelDetails();
        extractor.extractModelValidations();

        List<List<String>> pdxModelDetails = extractor.extractModelDetails();
        List<List<String>> pdxModelValidations = extractor.extractModelValidations();

        Assert.assertEquals("m123", pdxModelDetails.get(0).get(0));
        Assert.assertEquals("hsname", pdxModelDetails.get(0).get(1));

        Assert.assertEquals("m123", pdxModelValidations.get(0).get(0));
        Assert.assertEquals("technology", pdxModelValidations.get(0).get(1));

    }

    @Test
    public void Given_Provider_When_GetSharingAndContactSheetIsCalled_Then_SharingAndContactDataIsInRowOne(){
        when(dataImportService.findModelsWithSharingAndContactByDS(providerGroup.getAbbreviation()))
          .thenReturn(getModelListForTest());

        List<List<String>> sharingAndContact = extractor.extractSharingAndContact();

        Assert.assertEquals("m123", sharingAndContact.get(0).get(0));
        Assert.assertEquals("Academia", sharingAndContact.get(0).get(1));
    }

    @Test
    public void Given_Provider_When_GetLoaderRelatedSheetIsCalled_Then_DataIsInRowOne(){
        List<List<String>> loaderRelatedData = extractor.extractLoaderRelatedData();
        Assert.assertEquals("TG", loaderRelatedData.get(0).get(1));
    }

    @Test
    public void Given_ModelWithMolecularData_When_GetSamplePlatformSheetIsCalled_Then_SamplePlatformDataIsInRowOne(){

        when(dataImportService.findModelXenograftPlatformSampleByDS(providerGroup.getAbbreviation()))
          .thenReturn(getModelListForTest());

        List<List<String>> samplePlatformDescription = extractor.extractSamplePlatformDescription();

        Assert.assertEquals(patientSampleId, samplePlatformDescription.get(0).get(1));
        Assert.assertEquals(xenoSampleId, samplePlatformDescription.get(2).get(1));
    }

    @Test
    public void Given_XenograftSamplewithMutationAndCna_When_extractGroupOmicDataIsCalled_Then_returnAppropriateDataForEach(){
        List<ModelCreation> modelList = getModelListForTest();

        when(dataImportService.findModelsWithSharingAndContactByDS((providerGroup.getAbbreviation())))
                .thenReturn(modelList);

        when(dataImportService.findModelWithMolecularDataByDSAndIdAndMolcharType(
                providerGroup.getAbbreviation(),"m123", mutMolType))
                .thenReturn(modelList.get(0));

        when(dataImportService.findModelWithMolecularDataByDSAndIdAndMolcharType(
                providerGroup.getAbbreviation(),"m123", cnaMolType))
                .thenReturn(modelList.get(0));

        List<List<String>> mutationData = extractor.extractGroupOmicData(mutMolType);
        List<List<String>> cnaData = extractor.extractGroupOmicData(cnaMolType);
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

        when(dataImportService.findModelsWithSharingAndContactByDS((providerGroup.getAbbreviation())))
                .thenReturn(modelList);

        when(dataImportService.findModelWithMolecularDataByDSAndIdAndMolcharType(
                providerGroup.getAbbreviation(),"m123", mutMolType))
                .thenReturn(modelList.get(0));

        when(dataImportService.findModelWithMolecularDataByDSAndIdAndMolcharType(
                providerGroup.getAbbreviation(),"m123", cnaMolType))
                .thenReturn(modelList.get(0));

        List<List<String>> mutationData = extractor.extractGroupOmicData(mutMolType);
        List<List<String>> cnaData = extractor.extractGroupOmicData(cnaMolType);
        Assert.assertTrue(mutationData.get(0).contains(ncbiId));
        Assert.assertFalse(mutationData.get(0).contains(marker));
        Assert.assertTrue(cnaData.get(0).contains(marker));
        Assert.assertFalse(cnaData.get(0).contains(biotype));
    }


    @Test
    public void Given_ModelwithMutationData_When_extractModelDataIsCalledwithDifferentMolcType_Then_NoDataIsRetrieved(){
        List<ModelCreation> modelList = getModelListForTest();

        when(dataImportService.findModelsWithSharingAndContactByDS((providerGroup.getAbbreviation())))
                .thenReturn(modelList);

        when(dataImportService.findModelWithMolecularDataByDSAndIdAndMolcharType(
                providerGroup.getAbbreviation(),"m123", mutMolType))
                .thenReturn(modelList.get(0));

        List<List<String>> expressionData = extractor.extractGroupOmicData("expression");
        Assert.assertTrue(expressionData.size() == 0);
    }

    private List<ModelCreation> getModelListForTest(){
        List<ModelCreation> modelCreationList = new ArrayList<>();

        ModelCreation model = new ModelCreation();
        model.setSourcePdxId("m123");

        Group accessGroup = Group.createAccessibilityGroup("Academia", "transnational");
        Group project = new Group("project1", "p1", "Project");

        Group publicationGroup = new Group();
        publicationGroup.setType("Publication");
        publicationGroup.setPubMedId("12345");

        model.addGroup(publicationGroup);
        model.addGroup(providerGroup);
        model.addGroup(accessGroup);
        model.addGroup(project);

        ExternalUrl url = new ExternalUrl(ExternalUrl.Type.CONTACT,"email@address.com");
        List<ExternalUrl> urlList = new ArrayList<>();
        urlList.add(url);
        model.setExternalUrls(urlList);

        QualityAssurance qualityAssurance = new QualityAssurance("technology", "description", "1,2");
        model.addQualityAssurance(qualityAssurance);

        Sample patientSample = new Sample();
        patientSample.setSourceSampleId(patientSampleId);

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


}
