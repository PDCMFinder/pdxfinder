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

import static org.junit.Assert.fail;

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
    private static final String age = "99";

    private static final Group group = new Group("TEST","","TST");
    private static final Patient patient = new Patient(externalId, sex, race, ethnicity, group);

    private static final String ageAtCollection = "99";
    private static final String collectionDate = "TEST";
    private static final String collectionEvent = "TEST";
    private static final String elapsedTime = "TEST";

    private Patient patientWithSnapshots = new Patient(externalId, sex, race, ethnicity, group);
    private PatientSnapshot snapshot = new PatientSnapshot(patient, ageAtCollection,
            collectionDate, collectionEvent, elapsedTime);

    @Before
    public void init(){
    }

    @Test
    public void GetPatient_WhenValidDataNotInDB_ReturnNewPatient(){

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

        PatientSnapshot expectedSnapshot = new PatientSnapshot(patient, age);

        HashSet<PatientSnapshot> pSnaps = new HashSet<PatientSnapshot>();
        pSnaps.add(expectedSnapshot);

        when(this.patientSnapshotRepository.findByPatient(externalId))
                .thenReturn(pSnaps);

        PatientSnapshot returnSnapshot = dataImportService.getPatientSnapshot(patient, age);

        Assert.assertEquals(expectedSnapshot, returnSnapshot);
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
        PatientSnapshot snapshot = new PatientSnapshot(patient, ageAtCollection,
                collectionDate, collectionEvent, elapsedTime);

       PatientSnapshot testSnapshot;

       patientWithSnapshots.hasSnapshot(snapshot);
       snapshot.setPatient(patientWithSnapshots);

       testSnapshot = dataImportService.getPatientSnapshot(patientWithSnapshots, ageAtCollection,
                collectionDate, collectionEvent, elapsedTime);

       Assert.assertTrue(testSnapshot.equals(snapshot));


    }

    @Test
    public void GetPatientSnapshot4Param_WhenValidArgAndNoMatchingPS_ReturnNewPSforPatient(){

        PatientSnapshot snapshot = new PatientSnapshot(patient, ageAtCollection,
                collectionDate, collectionEvent, elapsedTime);

        PatientSnapshot testSnapshot;

        testSnapshot = dataImportService.getPatientSnapshot(patient, ageAtCollection,
                collectionDate, collectionEvent, elapsedTime);

        Assert.assertEquals(snapshot.getPatient(), testSnapshot.getPatient());
        Assert.assertEquals(snapshot.getAgeAtCollection(), testSnapshot.getAgeAtCollection());
        Assert.assertEquals(snapshot.getDateAtCollection(), testSnapshot.getDateAtCollection());
        Assert.assertEquals(snapshot.getCollectionEvent(), testSnapshot.getCollectionEvent());
        Assert.assertEquals(snapshot.getElapsedTime(), testSnapshot.getDateAtCollection());
    }

    @Test
    public void GetPatientSnapshot6Param_When_PatientInDB_ReturnNewPSforPatient(){

        when(patientRepository.findByExternalIdAndGroup(externalId, group))
                .thenReturn(patient);

        when(dataImportService.getPatientSnapshot(patient, age))
                .thenReturn(snapshot);

        PatientSnapshot patientSnapshot;

        patientSnapshot = dataImportService.getPatientSnapshot(externalId, sex, race,
                ethnicity, age, group);

        //Restricts method to creating a new patient since REF are checked. Could cause odd behavior.
        Assert.assertEquals(patientSnapshot.getPatient(),patient);
        Assert.assertEquals(patientSnapshot.getAgeAtCollection(), snapshot.getAgeAtCollection());
    }

    @Test
    public void GetPatientSnapshot6Param_When_ValidArgAndNoPatientInDB_ReturnMatchingPS(){

        when(patientRepository.findByExternalIdAndGroup(externalId, group))
                .thenReturn(null);

        when(dataImportService.getPatient(externalId, sex, race,
                ethnicity, group)).thenReturn(patient);

        PatientSnapshot testSnapshot;

        testSnapshot = dataImportService.getPatientSnapshot(externalId, sex, race,
                ethnicity, age, group);

        //It seems that the patient being returned is not the same reference Mockito is passing to it.
        //I am unclear to the origin of this behavior. Could be problmatic in the future.
        Assert.assertEquals(snapshot.getPatient().getExternalId(), testSnapshot.getPatient().getExternalId());
        Assert.assertEquals(snapshot.getAgeAtCollection(), testSnapshot.getAgeAtCollection());
    }

}


