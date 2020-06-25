package org.pdxfinder.dataloaders.updog.domainobjectcreation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pdxfinder.dataloaders.updog.TSV;
import org.pdxfinder.graph.dao.EngraftmentMaterial;
import org.pdxfinder.graph.dao.EngraftmentSite;
import org.pdxfinder.graph.dao.EngraftmentType;
import org.pdxfinder.graph.dao.HostStrain;
import org.pdxfinder.graph.dao.Specimen;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class SpecimenCreatorTest {

    @Mock private SampleCreator sampleCreator;
    @InjectMocks private SpecimenCreator specimenCreator;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test public void create_givenTableSet_createSpecimens() {
        Map<String, Table> testTableSet = DomainObjectCreatorTest.getTestPdxDataTables();
        Row row = testTableSet.get("metadata-model.tsv").row(0);
        String passageNumber = row.getString(TSV.Metadata.passage_number.name());

        Specimen expected = new Specimen();
        expected.setPassage(passageNumber);
        expected.setHostStrain(new HostStrain(
            row.getString(TSV.Metadata.host_strain.name()),
            row.getString(TSV.Metadata.host_strain_full.name())));
        expected.setEngraftmentMaterial(new EngraftmentMaterial(
            row.getString(TSV.Metadata.sample_type.name()),
            row.getString(TSV.Metadata.sample_state.name())));
        expected.setEngraftmentSite(new EngraftmentSite(
            row.getString(TSV.Metadata.engraftment_site.name())));
        expected.setEngraftmentType(new EngraftmentType(
            row.getString(TSV.Metadata.engraftment_type.name())));

        assertThat(specimenCreator.create(testTableSet).contains(expected), is(true));

    }
}