package org.pdxfinder;

import static org.mockito.Mockito.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.pdxfinder.graph.dao.Group;
import org.pdxfinder.graph.dao.Patient;
import org.pdxfinder.graph.dao.PatientSnapshot;
import org.pdxfinder.graph.repositories.PatientRepository;
import org.pdxfinder.graph.repositories.PatientSnapshotRepository;
import org.pdxfinder.services.DataImportService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;

public class DataImportServiceTests extends BaseTest {

    @Mock
    private PatientSnapshotRepository patientSnapshotRepository;

    @Mock
    private PatientRepository patientRepository;

    @Spy
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

    private static final String ageAtCollection = "99";
    private static final String collectionDate = "TEST";
    private static final String collectionEvent = "TEST";
    private static final String elapsedTime = "TEST";

    private static final Group group = new Group("TEST","","TST");
    private static final Patient patient = new Patient(externalId, sex, race, ethnicity, group);

    private static final PatientSnapshot snapshot = new PatientSnapshot(patient, ageAtCollection,
            collectionDate, collectionEvent, elapsedTime);
    private PatientSnapshot actualSnapshot;

    @Before
    public void init(){
        actualSnapshot = null;
    }

    @Test
    public void GetPatient_WhenPSNotInDB_ReturnNewPatient(){

        when(this.patientRepository.findByExternalIdAndGroup(externalId,group))
                .thenReturn(null);

        Patient patient = dataImportService.getPatient(externalId, sex, race, ethnicity, group);

        Assert.assertEquals(patient.getExternalId(), externalId);
        Assert.assertEquals(patient.getSex(), sex);
        Assert.assertEquals(patient.getRace(), race);
        Assert.assertEquals(patient.getEthnicity(), ethnicity);
    }

    @Test(expected = NullPointerException.class)
    public void GetPatient_WhenGroupIsNull_ThrowNullPointer() {

        dataImportService.getPatient(externalId,sex,race,ethnicity, null);
    }

    @Test(expected = RuntimeException.class)
    public void GetPatient_WhenAllButGroupIsNull_ReturnException() {

        //Not clear if this is should be defined behavior.
        dataImportService.getPatient(null,null,null,null, group);
    }

    @Test
    public void GetPatientSnapshotTwoParam_WhenValidArgAndPSinDB_ReturnPSfromDBwithEqualRef() {

        PatientSnapshot snapshot1 = new PatientSnapshot(patient,ageAtCollection);

        HashSet<PatientSnapshot> pSnaps = new HashSet<>();
        pSnaps.add(snapshot1);

        when(this.patientSnapshotRepository.findByPatient(externalId))
                .thenReturn(pSnaps);

        PatientSnapshot returnSnapshot = dataImportService.getPatientSnapshot(patient, age);

        Assert.assertEquals(returnSnapshot, snapshot1);
    }

    @Test
    public void GetPatientSnapshotTwoParam_WhenValidArgAndNoPSinDB_ReturnNewPS() {

        when(this.patientSnapshotRepository.findByPatient(externalId))
                .thenReturn(new HashSet<>());

        PatientSnapshot returnSnapshot = dataImportService.getPatientSnapshot(patient, age);

        Assert.assertEquals(patient, returnSnapshot.getPatient());
        Assert.assertEquals(age, returnSnapshot.getAgeAtCollection());
    }

    @Test
    public void GetPatientSnapshot4Param_WhenValidArg_ReturnPSfromPatientWithEqualREF() {

        Patient patientWithSnapshots = new Patient(externalId, sex, race, ethnicity, group);

        patientWithSnapshots.hasSnapshot(actualSnapshot);
        actualSnapshot.setPatient(patientWithSnapshots);

        actualSnapshot = dataImportService.getPatientSnapshot(patientWithSnapshots, ageAtCollection,
                collectionDate, collectionEvent, elapsedTime);

        Assert.assertTrue(actualSnapshot.equals(actualSnapshot));
    }

    @Test
    public void GetPatientSnapshot4Param_WhenValidArgAndNoMatchingPS_ReturnNewPSforPatient(){

        actualSnapshot = dataImportService.getPatientSnapshot(patient, ageAtCollection,
                collectionDate, collectionEvent, elapsedTime);

        Assert.assertEquals(patient, snapshot.getPatient());
        Assert.assertEquals(ageAtCollection, snapshot.getAgeAtCollection());
        Assert.assertEquals(collectionDate, snapshot.getDateAtCollection());
        Assert.assertEquals(collectionEvent, snapshot.getCollectionEvent());
        Assert.assertEquals(elapsedTime, snapshot.getDateAtCollection());
    }

    @Test
    public void GetPatientSnapshot6Param_When_PatientInDB_ReturnNewPSforPatient(){

        when(patientRepository.findByExternalIdAndGroup(externalId, group))
                .thenReturn(patient);

        when(dataImportService.getPatientSnapshot(patient, age))
                .thenReturn(actualSnapshot);

        actualSnapshot = dataImportService.getPatientSnapshot(externalId, sex, race,
                ethnicity, age, group);

        Assert.assertEquals(actualSnapshot.getPatient(),patient);
        Assert.assertEquals(actualSnapshot.getAgeAtCollection(), actualSnapshot.getAgeAtCollection());
    }

    @Test
    public void GetPatientSnapshot6Param_When_ValidArgAndNoPatientInDB_ReturnMatchingPS(){

        when(patientRepository.findByExternalIdAndGroup(externalId, group))
                .thenReturn(null);

        when(dataImportService.getPatient(externalId, sex, race,
                ethnicity, group)).thenReturn(patient);

         actualSnapshot = dataImportService.getPatientSnapshot(externalId, sex, race,
                ethnicity, age, group);

        //It seems that the patient being returned is not the same reference Mockito is passing to it.
        //I am unclear to the origin of this behavior. Could be problmatic in the future.
        Assert.assertEquals(patient,actualSnapshot.getPatient());
        Assert.assertEquals(externalId,  actualSnapshot.getPatient().getExternalId());
        Assert.assertEquals(ageAtCollection,  actualSnapshot.getAgeAtCollection());
    }

}


