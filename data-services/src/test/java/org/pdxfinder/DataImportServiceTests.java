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
    @Autowired
    private DataImportService dataImportService;

    private static final String EXTERNAL_ID = "1";
    private static final String SEX = "Male";
    private static final String RACE = "Asian";
    private static final String ETHNICITY = "German";

    private static final String AGE_AT_COLLECTION = "99";
    private static final String COLLECTION_DATE = "TEST";
    private static final String COLLECTION_EVENT = "TEST";
    private static final String ELAPSED_TIME = "TEST";

    private static final Group GROUP = new Group("TEST","","TST");
    private static final Patient PATIENT = new Patient(EXTERNAL_ID, SEX, RACE, ETHNICITY, GROUP);

    private static final PatientSnapshot SNAPSHOT = new PatientSnapshot(PATIENT, AGE_AT_COLLECTION,
            COLLECTION_DATE, COLLECTION_EVENT, ELAPSED_TIME);
    private PatientSnapshot actualSnapshot;

    @Before
    public void init(){
        actualSnapshot = null;
    }

    @Test
    public void Given_GetPatientSnapshotTwoParam_When_ValidArgAndPSinDB_Then_returnPSfromDBwithEqualRef() {

        PatientSnapshot expectedSnapshot = new PatientSnapshot(PATIENT, AGE_AT_COLLECTION);

        HashSet<PatientSnapshot> pSnaps = new HashSet<>();
        pSnaps.add(expectedSnapshot);

        when(this.patientSnapshotRepository.findByPatient(EXTERNAL_ID))
                .thenReturn(pSnaps);

        actualSnapshot = dataImportService.getPatientSnapshot(PATIENT, AGE_AT_COLLECTION);

        Assert.assertEquals(expectedSnapshot, expectedSnapshot);
    }

    @Test
    public void Given_GetPatientSnapshotTwoParam_When_ValidArgAndNoPSinDB_Then_ReturnNewPS() {

        when(this.patientSnapshotRepository.findByPatient(EXTERNAL_ID))
                .thenReturn(new HashSet<>());

        actualSnapshot = dataImportService.getPatientSnapshot(PATIENT, AGE_AT_COLLECTION);

        Assert.assertEquals(PATIENT, actualSnapshot.getPatient());
        Assert.assertEquals(AGE_AT_COLLECTION, actualSnapshot.getAgeAtCollection());
    }

    @Test
    public void Given_GetPatientSnapshot4Param_When_ValidArg_Then_ReturnPSfromPatientWithEqualREF() {

        Patient patientWithSnapshots = new Patient(EXTERNAL_ID, SEX, RACE, ETHNICITY, GROUP);

        patientWithSnapshots.hasSnapshot(SNAPSHOT);
        SNAPSHOT.setPatient(patientWithSnapshots);

        actualSnapshot = dataImportService.getPatientSnapshot(patientWithSnapshots, AGE_AT_COLLECTION,
                COLLECTION_DATE, COLLECTION_EVENT, ELAPSED_TIME);

        Assert.assertEquals(SNAPSHOT, actualSnapshot);
    }


    @Test
    public void Given_GetPatientSnapshot4Param_When_ValidArgAndNoMatchingPS_Then_ReturnNewPSforPatient(){

        actualSnapshot = dataImportService.getPatientSnapshot(PATIENT, AGE_AT_COLLECTION,
                COLLECTION_DATE, COLLECTION_EVENT, ELAPSED_TIME);

        Assert.assertEquals(PATIENT, actualSnapshot.getPatient());
        Assert.assertEquals(AGE_AT_COLLECTION, actualSnapshot.getAgeAtCollection());
        Assert.assertEquals(COLLECTION_DATE, actualSnapshot.getDateAtCollection());
        Assert.assertEquals(COLLECTION_EVENT, actualSnapshot.getCollectionEvent());
        Assert.assertEquals(ELAPSED_TIME, actualSnapshot.getDateAtCollection());
    }

    @Test
    public void Given_GetPatientSnapshot6Param_When_PatientInDB_Then_ReturnNewPSforPatient(){

        when(patientRepository.findByExternalIdAndGroup(EXTERNAL_ID, GROUP))
                .thenReturn(PATIENT);

        when(dataImportService.getPatientSnapshot(PATIENT, AGE_AT_COLLECTION))
                .thenReturn(actualSnapshot);

        actualSnapshot = dataImportService.getPatientSnapshot(EXTERNAL_ID, SEX, RACE,
                ETHNICITY, AGE_AT_COLLECTION, GROUP);

        Assert.assertEquals(PATIENT, actualSnapshot.getPatient());
        Assert.assertEquals(AGE_AT_COLLECTION, actualSnapshot.getAgeAtCollection());
    }

    @Test
    public void Given_getPatientSnapshot6Param_When_ValidArgAndNoPatientInDB_Then_ReturnMatchingPS(){

        when(patientRepository.findByExternalIdAndGroup(EXTERNAL_ID, GROUP))
                .thenReturn(null);

         actualSnapshot = dataImportService.getPatientSnapshot(EXTERNAL_ID, SEX, RACE,
                 ETHNICITY, AGE_AT_COLLECTION, GROUP);

        Assert.assertEquals(EXTERNAL_ID,  actualSnapshot.getPatient().getExternalId());
        Assert.assertEquals(AGE_AT_COLLECTION,  actualSnapshot.getAgeAtCollection());
    }

    @Test(expected = NullPointerException.class)
    public void Given_GetPatientSnapshot6Param_When_ExternalIdIsBlank_Then_ReturnNullException(){

        actualSnapshot = dataImportService.getPatientSnapshot("", SEX, RACE,ETHNICITY,
                AGE_AT_COLLECTION, GROUP);

    }

    @Test(expected = NullPointerException.class)
    public void Given_GetPatientSnapshot6Param_When_ExternalIdIsNull_Then_ReturnNullException(){

        actualSnapshot = dataImportService.getPatientSnapshot(null, SEX, RACE,ETHNICITY,
                AGE_AT_COLLECTION, GROUP);

    }


}


