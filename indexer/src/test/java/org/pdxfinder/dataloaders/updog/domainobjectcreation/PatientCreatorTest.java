package org.pdxfinder.dataloaders.updog.domainobjectcreation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pdxfinder.graph.dao.Group;
import org.pdxfinder.graph.dao.Patient;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class PatientCreatorTest {

    private Map<String, Table> testTableSet = DomainObjectCreatorTest.getTestPdxDataTables();
    private Row PATIENT_ROW = testTableSet.get("metadata-patient.tsv").row(0);
    private String EXTERNAL_ID = PATIENT_ROW.getText("patient_id");
    private String SEX = PATIENT_ROW.getText("sex");
    private String ETHNICITY = PATIENT_ROW.getText("ethnicity");
    private String HISTORY = PATIENT_ROW.getText("history");
    private String INITIAL_DIAGNOSIS = PATIENT_ROW.getText("initial_diagnosis");
    private String AGE_AT_INITIAL_DIAGNOSIS = PATIENT_ROW.getText("age_at_initial_diagnosis");


    @Mock private Group providerGroup;
    @InjectMocks private PatientCreator patientCreator;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test public void create_givenValidTable_createPatient() {
        Map<String, Table> testTableSet = DomainObjectCreatorTest.getTestPdxDataTables();
        Patient expected = new Patient(EXTERNAL_ID, providerGroup);
        expected.setSex(SEX);
        expected.setEthnicity(ETHNICITY);
        expected.setCancerRelevantHistory(HISTORY);
        expected.setFirstDiagnosis(INITIAL_DIAGNOSIS);
        expected.setAgeAtFirstDiagnosis(AGE_AT_INITIAL_DIAGNOSIS);

        assertThat(patientCreator.create(testTableSet, providerGroup).contains(expected), is(true));
    }

}