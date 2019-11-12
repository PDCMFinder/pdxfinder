package org.pdxfinder.dataexport;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pdxfinder.BaseTest;
import org.pdxfinder.graph.dao.*;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.springframework.boot.test.mock.mockito.MockBean;
import java.util.ArrayList;
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

        Patient patient = new Patient("p123", "male", "", "", providerGroup);
        patient.setCancerRelevantHistory("");
        patient.setFirstDiagnosis("");
        patient.setAgeAtFirstDiagnosis("60");

        List<Patient> patientList = new ArrayList<>();
        patientList.add(patient);

        when(dataImportService.findPatientsByGroup(providerGroup)).thenReturn(patientList);

        universalDataExporter.init("", providerGroup);
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

        universalDataExporter.init("", providerGroup);

        List<List<String>> patientTumorAtCollection = universalDataExporter.getPatientTumorSheetData();
        Assert.assertEquals("p123", patientTumorAtCollection.get(0).get(0));
    }

    @Test
    public void TestPdxModelDetailsAndValidations(){

        ModelCreation model = new ModelCreation();
        model.setSourcePdxId("m123");
        HostStrain hostStrain = new HostStrain("HostSymbol", "HostName");

        Specimen specimen = new Specimen();
        specimen.setPassage("1");
        specimen.setHostStrain(hostStrain);
        model.addSpecimen(specimen);

        QualityAssurance qualityAssurance = new QualityAssurance("technology", "description", "1,2");
        model.addQualityAssurance(qualityAssurance);

        List<ModelCreation> modelCreationList = new ArrayList<>();
        modelCreationList.add(model);

        when( dataImportService.findModelsWithSpecimensAndQAByDS(providerGroup.getAbbreviation())).thenReturn(modelCreationList);

        universalDataExporter.init("", providerGroup);
        List<List<String>> pdxModelDetails = universalDataExporter.getPdxModelSheetData();
        List<List<String>> pdxModelValidations = universalDataExporter.getPdxModelValidationSheetData();


        Assert.assertEquals("m123", pdxModelDetails.get(0).get(0));
        Assert.assertEquals("HostName", pdxModelDetails.get(0).get(1));

        Assert.assertEquals("m123", pdxModelValidations.get(0).get(0));
        Assert.assertEquals("technology", pdxModelValidations.get(0).get(1));

    }

    @Test
    public void TestSharingAndContact(){

        ModelCreation model = new ModelCreation();
        model.setSourcePdxId("m123");

        Group accessGroup = new Group("Academia", "transnational");
        Group project = new Group("project1", "p1", "Project");

        model.addGroup(providerGroup);
        model.addGroup(accessGroup);
        model.addGroup(project);

        ExternalUrl url = new ExternalUrl(ExternalUrl.Type.CONTACT,"email@address.com");
        List<ExternalUrl> urlList = new ArrayList<>();
        urlList.add(url);
        model.setExternalUrls(urlList);

        List<ModelCreation> modelCreationList = new ArrayList<>();
        modelCreationList.add(model);

        when(  dataImportService.findModelsWithSharingAndContactByDS(providerGroup.getAbbreviation())).thenReturn(modelCreationList);

        universalDataExporter.init("", providerGroup);
        List<List<String>> sharingAndContact = universalDataExporter.getSharingAndContactSheetData();

        Assert.assertEquals("m123", sharingAndContact.get(0).get(0));
        Assert.assertEquals("Academia", sharingAndContact.get(0).get(1));

    }



}
