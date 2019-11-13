package org.pdxfinder.dataexport;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.udf.UDFFinder;
import org.apache.poi.ss.usermodel.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pdxfinder.BaseTest;
import org.pdxfinder.graph.dao.*;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.mockito.Mockito.when;

public class DataExportTest extends BaseTest {


    @MockBean
    private DataImportService dataImportService;
    @MockBean
    protected UtilityService utilityService;

    Group providerGroup;
    UniversalDataExporter universalDataExporter;

    @Before
    public void setUp() {

        providerGroup = new Group("TestGroup", "TG", "", "Academia", "Bob", "Bob's page");

        universalDataExporter = new UniversalDataExporter(dataImportService, utilityService);
        //universalDataExporter.init("", providerGroup);
    }

    @Test
    public void TestPatient() {

        when(dataImportService.findPatientsByGroup(providerGroup)).thenReturn(getPatientListForTest());

        universalDataExporter.setDs(providerGroup);
        universalDataExporter.initPatientData();
        initWorkbooks();
        universalDataExporter.updateWorkbooks();
        List<List<String>> patientData = universalDataExporter.getPatientSheetData();

        Assert.assertEquals("p123", patientData.get(0).get(0));
    }

    @Test
    public void TestPatientTumorAtCollection(){

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

        when(dataImportService.findPatientTumorAtCollectionDataByDS(providerGroup)).thenReturn(patientList);
        when(dataImportService.findModelBySample(sample)).thenReturn(modelCreation);

        universalDataExporter.setDs(providerGroup);
        universalDataExporter.initPatientTumorAtCollection();
        universalDataExporter.updateWorkbooks();

        List<List<String>> patientTumorAtCollection = universalDataExporter.getPatientTumorSheetData();
        Assert.assertEquals("p123", patientTumorAtCollection.get(0).get(0));
    }

    @Test
    public void TestPdxModelDetailsAndValidations(){

        when( dataImportService.findModelsWithSpecimensAndQAByDS(providerGroup.getAbbreviation())).thenReturn(getModelListForTest());

        universalDataExporter.setDs(providerGroup);
        universalDataExporter.initPdxModelDetails();
        universalDataExporter.initPdxModelValidations();
        universalDataExporter.updateWorkbooks();

        List<List<String>> pdxModelDetails = universalDataExporter.getPdxModelSheetData();
        List<List<String>> pdxModelValidations = universalDataExporter.getPdxModelValidationSheetData();

        Assert.assertEquals("m123", pdxModelDetails.get(0).get(0));
        Assert.assertEquals("hsname", pdxModelDetails.get(0).get(1));

        Assert.assertEquals("m123", pdxModelValidations.get(0).get(0));
        Assert.assertEquals("technology", pdxModelValidations.get(0).get(1));

    }

    @Test
    public void TestSharingAndContact(){

        when(dataImportService.findModelsWithSharingAndContactByDS(providerGroup.getAbbreviation())).thenReturn(getModelListForTest());

        universalDataExporter.setDs(providerGroup);
        universalDataExporter.initSharingAndContact();
        universalDataExporter.updateWorkbooks();

        List<List<String>> sharingAndContact = universalDataExporter.getSharingAndContactSheetData();

        Assert.assertEquals("m123", sharingAndContact.get(0).get(0));
        Assert.assertEquals("Academia", sharingAndContact.get(0).get(1));

    }

    @Test
    public void TestLoaderRelatedData(){

        universalDataExporter.setDs(providerGroup);
        universalDataExporter.initLoaderRelatedData();
        universalDataExporter.updateWorkbooks();

        List<List<String>> loaderRelatedData = universalDataExporter.getLoaderRelatedDataSheetData();

        Assert.assertEquals("TG", loaderRelatedData.get(0).get(1));


    }

    @Test
    public void TestSamplePlatformDescription(){

        when(dataImportService.findModelXenograftPlatformSampleByDS(providerGroup.getAbbreviation())).thenReturn(getModelListForTest());

        universalDataExporter.setDs(providerGroup);
        universalDataExporter.initSamplePlatformDescription();
        universalDataExporter.updateWorkbooks();

        List<List<String>> samplePlatformDescription = universalDataExporter.getSamplePlatformDescriptionSheetData();

        Assert.assertEquals("s123", samplePlatformDescription.get(0).get(0));
        Assert.assertEquals("xs123", samplePlatformDescription.get(1).get(0));

    }

    @Test
    public void TestGenomicData(){

        when(dataImportService.findModelsWithSharingAndContactByDS(providerGroup.getAbbreviation())).thenReturn(getModelListForTest());
        when(dataImportService.findModelWithMolecularDataByDSAndIdAndMolcharType(providerGroup.getAbbreviation(), "m123", "mutation")).thenReturn(getModelListForTest().get(0));
        when(dataImportService.findModelWithMolecularDataByDSAndIdAndMolcharType(providerGroup.getAbbreviation(), "m123", "copy number alteration")).thenReturn(getModelListForTest().get(0));

        universalDataExporter.setDs(providerGroup);
        universalDataExporter.initMutationData();
        universalDataExporter.initCNAData();
        universalDataExporter.updateWorkbooks();

        List<List<String>> mutationData = universalDataExporter.getMutationSheetData();
        List<List<String>> cnaData = universalDataExporter.getCnaSheetData();

        Assert.assertEquals("m123", mutationData.get(0).get(1));
        Assert.assertEquals("markersymbol", mutationData.get(0).get(6));

        Assert.assertEquals("m123", cnaData.get(0).get(1));
        Assert.assertEquals("markersymbol", cnaData.get(0).get(6));

    }



    private List<ModelCreation> getModelListForTest(){

        ModelCreation model = new ModelCreation();
        model.setSourcePdxId("m123");

        Group accessGroup = new Group("Academia", "transnational");
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
        patientSample.setSourceSampleId("s123");

        model.setSample(patientSample);

        MolecularCharacterization molecularCharacterization = new MolecularCharacterization();
        molecularCharacterization.setType("mutation");
        molecularCharacterization.setTechnology("techtest");
        Platform platform = new Platform();
        platform.setName("platform");
        platform.setUrl("platformurl");

        molecularCharacterization.setPlatform(platform);
        patientSample.addMolecularCharacterization(molecularCharacterization);

        //setting up the xeno sample
        Specimen specimen = new Specimen();
        specimen.setPassage("1");
        specimen.setHostStrain(new HostStrain("hssymbol", "hsname"));

        Sample xenoSample = new Sample();
        xenoSample.setSourceSampleId("xs123");
        specimen.setSample(xenoSample);
        xenoSample.addMolecularCharacterization(molecularCharacterization);
        model.addSpecimen(specimen);

        List<ModelCreation> modelCreationList = new ArrayList<>();
        modelCreationList.add(model);


        //setting up mutation data
        MarkerAssociation ma = new MarkerAssociation();
        ma.setAminoAcidChange("aminoacidchange");
        Marker m = new Marker("markersymbol", "markername");
        ma.setMarker(m);
        molecularCharacterization.addMarkerAssociation(ma);

        //setting up cna data
        MolecularCharacterization molecularCharacterization2 = new MolecularCharacterization();
        molecularCharacterization2.setType("copy number alteration");
        MarkerAssociation ma2 = new MarkerAssociation();
        ma2.setCnaCopyNumberStatus("cnaStatus");
        Marker m2 = new Marker("markersymbol", "markername");
        ma2.setMarker(m2);
        molecularCharacterization2.addMarkerAssociation(ma2);
        xenoSample.addMolecularCharacterization(molecularCharacterization2);

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

    private void initWorkbooks(){



    }

}
