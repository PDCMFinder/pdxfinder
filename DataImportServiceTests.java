package org.pdxfinder.services;

import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.pdxfinder.BaseTest;
import org.pdxfinder.graph.dao.Group;
import org.pdxfinder.graph.dao.Patient;
import org.pdxfinder.graph.dao.PatientSnapshot;
import org.pdxfinder.graph.repositories.PatientRepository;
import org.pdxfinder.graph.repositories.PatientSnapshotRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;

import static org.junit.Assert.fail;

public class DataImportServiceTests extends BaseTest {

    @Mock
    private PatientSnapshotRepository patientSnapshotRepository;

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private DataImportService dataImportService;

    @Autowired
    void DataImportServiceTests(DataImportService dataImportService){
       this.dataImportService = dataImportService;
    }

    private static final String externalId = "1";
    private static final String sex = "Male";
    private static final String race = "Asian";
    private static final String ethnicity = "German";
    private static final String age = "99";

    private static final Group group = new Group("TEST","","TST");
    private static final Patient patient = new Patient(externalId, sex, race, ethnicity, group);

    private static final String ageAtCollection = "99";
    private static final String collectionDate = "TEST";
    private static final String collectionEvent = "TEST";
    private static final String ellapsedTime = "TEST";

    private Patient patientWithSnapshots = new Patient(externalId, sex, race, ethnicity, group);
    private PatientSnapshot snapshot = new PatientSnapshot(patient, ageAtCollection,
            collectionDate, collectionEvent, ellapsedTime);

    @Before
    public void init(){
    }

    @Test
    public void validateGetPatient(){

        when(this.patientRepository.findByExternalIdAndGroup(externalId,group))
                .thenReturn(null);

        Patient patient = dataImportService.getPatient(externalId, sex, race, ethnicity, group);

        Assert.assertEquals(patient.getExternalId(), externalId);
        Assert.assertEquals(patient.getSex(), sex);
        Assert.assertEquals(patient.getRace(), race);
        Assert.assertEquals(patient.getEthnicity(), ethnicity);
    }

    @Test(expected = NullPointerException.class)
    public void testGetPatientForNullException() {

        dataImportService.getPatient(externalId,sex,race,ethnicity, null);
    }

    @Test
    public void validateGetPatientSnapshotForTwoParameters() {

        HashSet<PatientSnapshot> pSnaps = new HashSet<PatientSnapshot>();
        pSnaps.add(new PatientSnapshot(patient, age));

        when(this.patientSnapshotRepository.findByPatient(externalId))
                .thenReturn(pSnaps);

        PatientSnapshot returnSnapshot = dataImportService.getPatientSnapshot(patient, age);

        Assert.assertEquals(patient, returnSnapshot.getPatient());
        Assert.assertEquals(age, returnSnapshot.getAgeAtCollection());
    }

    @Test
    public void validateGetPatientSnapshotForTwoParametersNoSnapshots() {

        when(this.patientSnapshotRepository.findByPatient(externalId))
                .thenReturn(new HashSet<>());

        PatientSnapshot returnSnapshot = dataImportService.getPatientSnapshot(patient, age);

        Assert.assertEquals(patient, returnSnapshot.getPatient());
        Assert.assertEquals(age, returnSnapshot.getAgeAtCollection());

    }

    @Test
    public void validateGetPatientSnapshot4Parameters() {

        Patient patientWithSnapshots = new Patient(externalId, sex, race, ethnicity, group);
        PatientSnapshot snapshot = new PatientSnapshot(patient, ageAtCollection,
                collectionDate, collectionEvent, ellapsedTime);

       PatientSnapshot testSnapshot;

       patientWithSnapshots.hasSnapshot(snapshot);
       snapshot.setPatient(patientWithSnapshots);

       testSnapshot = dataImportService.getPatientSnapshot(patientWithSnapshots, ageAtCollection,
                collectionDate, collectionEvent, ellapsedTime);

       Assert.assertTrue(testSnapshot.equals(snapshot));


    }

    @Test
    public void validateGetPatientSnapshot4ParametersNoMatchingSnapshots(){

        PatientSnapshot snapshot = new PatientSnapshot(patient, ageAtCollection,
                collectionDate, collectionEvent, ellapsedTime);

        Patient patient2 = patient;
        snapshot.setPatient(patient2);
        patient2.hasSnapshot(snapshot);

        PatientSnapshot testSnapshot;

        testSnapshot = dataImportService.getPatientSnapshot(patient, ageAtCollection,
                collectionDate, collectionEvent, ellapsedTime);

        //This is incorrect
        Assert.assertEquals(testSnapshot, snapshot);
    }

    @Test
    public void validateOutputGetPatientSnapshot6Param(){

        when(patientRepository.findByExternalIdAndGroup(externalId, group))
                .thenReturn(patient);

        when(this.patientSnapshotRepository.findByPatient(externalId))
                .thenReturn(new HashSet<>());

        PatientSnapshot patientSnapshot;

        patientSnapshot = dataImportService.getPatientSnapshot(externalId, sex, race,
                ethnicity, age, group);

        Assert.assertEquals(patientSnapshot.getAgeAtCollection(), snapshot.getAgeAtCollection());
        Assert.assertEquals(patientSnapshot.getPatient(),patient);
    }

    @Test
    public void validateOutputGetPatientSnapshot6ParamWithoutSnapshot(){

        when(patientRepository.findByExternalIdAndGroup(externalId, group))
                .thenReturn(null)
                .thenReturn(patient);

        PatientSnapshot patientSnapshot;

        patientSnapshot = dataImportService.getPatientSnapshot(externalId, sex, race,
                ethnicity, age, group);

        Assert.assertEquals(patientSnapshot.getAgeAtCollection(), snapshot.getAgeAtCollection());
        Assert.assertEquals(patientSnapshot.getPatient(),patient);
    }

}


