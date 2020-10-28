package org.pdxfinder.dataloaders.updog.domainobjectcreation;

import org.pdxfinder.TSV;
import org.pdxfinder.graph.dao.Group;
import org.pdxfinder.graph.dao.ModelCreation;
import org.pdxfinder.graph.dao.Sample;
import org.pdxfinder.graph.dao.Specimen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class ModelCreationCreator {

    private SpecimenCreator specimenCreator;
    private SampleCreator sampleCreator;
    private static final Logger log = LoggerFactory.getLogger(ModelCreationCreator.class);

    public ModelCreationCreator (SpecimenCreator specimenCreator, SampleCreator sampleCreator) {
        this.specimenCreator = specimenCreator;
        this.sampleCreator = sampleCreator;
    }

    public Set<Sample> createDependencies(Map<String, Table> tableSet) {
        return this.sampleCreator.createPatientSample(tableSet);
    }

    public Set<Specimen> createDependencies(Map<String, Table> tableSet, Set<Sample> samples) {
        return specimenCreator.create(tableSet);
    }

    public Set<ModelCreation> create(
        Map<String, Table> tableSet,
        Group providerGroup,
        Set<Specimen> specimens
        ) {
        log.debug("Creating model data");

        Set<ModelCreation> modelCreations = new HashSet<>();
        Table modelTable = tableSet.get("metadata-model.tsv");

        for (Row row : modelTable) {
            String modelId = row.getString(TSV.Metadata.model_id.name());
            String hostStrainNomenclature = row.getString(TSV.Metadata.host_strain_full.name());
            String passageNumber = row.getString(TSV.Metadata.passage_number.name());

            Specimen specimen = getSpecimenFromSet(passageNumber, hostStrainNomenclature, specimens);

            ModelCreation modelCreation = new ModelCreation();
            modelCreation.setSourcePdxId(modelId);
            modelCreation.setDataSource(providerGroup.getAbbreviation());
            modelCreation.addSpecimen(specimen);

            modelCreations.add(modelCreation);
        }

        return modelCreations;
    }

    public Specimen getSpecimenFromSet(String passageNumber, String hostStrainNomenclature, Set<Specimen> specimenSet) {
        for (Specimen s : specimenSet) {
            if (s.getPassage().equals(passageNumber) &&
                s.getHostStrain().getSymbol().equals(hostStrainNomenclature)) return s;
        }
        return new Specimen();
    }

}
