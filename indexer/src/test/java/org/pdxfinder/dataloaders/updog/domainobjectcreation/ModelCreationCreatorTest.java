package org.pdxfinder.dataloaders.updog.domainobjectcreation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.pdxfinder.dataloaders.updog.TSV;
import org.pdxfinder.graph.dao.*;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class ModelCreationCreatorTest {

    @Mock private Sample sample;
    @Mock private Group providerGroup;
    @InjectMocks private ModelCreationCreator modelCreationCreator;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void create_givenSpecimenInTableSet_createsModelCreationWithSpecimen() {
        Map<String, Table> testTableSet = DomainObjectCreatorTest.getTestPdxDataTables();
        Row modelRow = testTableSet.get("metadata-model.tsv").row(0);
        String modelId = modelRow.getString(TSV.Metadata.model_id.name());

        String hostStrainNomenclature = modelRow.getString(TSV.Metadata.host_strain_full.name());
        String passageNumber = modelRow.getString(TSV.Metadata.passage_number.name());

        Specimen specimen = new Specimen();
        specimen.setPassage(passageNumber);
        specimen.setHostStrain(new HostStrain(hostStrainNomenclature));

        Set<Specimen> specimens = new HashSet<>();
        specimens.add(specimen);

        ModelCreation expected = new ModelCreation();
        expected.setSourcePdxId(modelId);
        expected.setDataSource(providerGroup.getAbbreviation());
        expected.addSpecimen(specimen);
        expected.setSample(sample);

        assertThat(modelCreationCreator.create(testTableSet, providerGroup, specimens).contains(expected), is(true));
    }
}