package org.pdxfinder.dataloaders.updog.domainobjectcreation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pdxfinder.dataloaders.updog.TSV;
import org.pdxfinder.graph.dao.Sample;
import org.pdxfinder.graph.dao.Tissue;
import org.pdxfinder.graph.dao.TumorType;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class SampleCreatorTest {

    @Mock private TumorType tumorType;
    @Mock private Tissue tissue;
    @InjectMocks private SampleCreator sampleCreator;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test public void createPatientSample_givenTableSet_createsSamples() {
        Map<String, Table> testTableSet = DomainObjectCreatorTest.getTestPdxDataTables();
        Row row = testTableSet.get("metadata-sample.tsv").row(0);

        Sample expected = new Sample(row.getString(TSV.Metadata.sample_id.name()));
        expected.setType(tumorType);
        expected.setSampleSite(tissue);
        expected.setOriginTissue(tissue);
        expected.setDiagnosis(row.getString(TSV.Metadata.diagnosis.name()));
        expected.setStage(row.getString(TSV.Metadata.stage.name()));
        expected.setStageClassification(row.getString(TSV.Metadata.staging_system.name()));
        expected.setGrade(row.getString(TSV.Metadata.grade.name()));
        expected.setGradeClassification(row.getString(TSV.Metadata.grading_system.name()));

        assertThat(sampleCreator.createPatientSample(testTableSet).contains(expected), is(true));

    }
}