package org.pdxfinder.dataexport;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.pdxfinder.BaseTest;
import org.pdxfinder.graph.dao.*;
import org.pdxfinder.graph.repositories.*;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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
        //when(dataImportService.findModelBySample(sample).getSourcePdxId()).thenReturn(modelCreation.getSourcePdxId());

        universalDataExporter.init("", providerGroup);

        List<List<String>> patientTumorAtCollection = universalDataExporter.getPatientTumorSheetData();
        Assert.assertEquals("p123", patientTumorAtCollection.get(0).get(0));

    }


}
