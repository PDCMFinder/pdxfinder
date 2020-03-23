package org.pdxfinder.dataloaders.updog.domainobjectcreation;

import org.pdxfinder.dataloaders.updog.TSV;
import org.pdxfinder.graph.dao.Sample;
import org.pdxfinder.graph.dao.Tissue;
import org.pdxfinder.graph.dao.TumorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class SampleCreator {
    private static final Logger log = LoggerFactory.getLogger(SampleCreator.class);

    public Set<Sample> createPatientSample(Map<String, Table> tableSet) {
        Set<Sample> samples = new HashSet<>();
        Set<Tissue> tissues = new HashSet<>();
        Set<TumorType> tumorTypes = new HashSet<>();
        Table sampleTable = tableSet.get("metadata-sample.tsv");

        for (Row row : sampleTable) {
            Tissue primarySite = new Tissue(row.getString(TSV.Metadata.primary_site.name()));
            Tissue collectionSite = new Tissue(row.getString(TSV.Metadata.collection_site.name()));
            TumorType tumorType = new TumorType(row.getString(TSV.Metadata.tumour_type.name()));

            Sample s = new Sample();
            s.setType(tumorType);
            s.setSampleSite(collectionSite);
            s.setOriginTissue(primarySite);
            s.setSourceSampleId(row.getString(TSV.Metadata.sample_id.name()));
            s.setDiagnosis(row.getString(TSV.Metadata.diagnosis.name()));
            s.setStage(row.getString(TSV.Metadata.stage.name()));
            s.setStageClassification(row.getString(TSV.Metadata.staging_system.name()));
            s.setGrade(row.getString(TSV.Metadata.grade.name()));
            s.setGradeClassification(row.getString(TSV.Metadata.grading_system.name()));

            samples.add(s);
            tissues.add(primarySite);
            tissues.add(collectionSite);
            tumorTypes.add(tumorType);
        }
        return samples;
    }

    public Sample createHostSampleForSpecimen(String modelId, String passage) {
        return new Sample(String.format("%s-%s", modelId, passage));
    }
}
