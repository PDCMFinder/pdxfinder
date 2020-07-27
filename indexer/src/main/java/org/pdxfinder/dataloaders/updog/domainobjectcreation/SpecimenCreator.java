package org.pdxfinder.dataloaders.updog.domainobjectcreation;

import org.pdxfinder.TSV;
import org.pdxfinder.graph.dao.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class SpecimenCreator {
    private static final Logger log = LoggerFactory.getLogger(Specimen.class);
    private SampleCreator sampleCreator;

    SpecimenCreator(SampleCreator sampleCreator) {
        this.sampleCreator = sampleCreator;
    }

    Set<Specimen> create(Map<String, Table> tableSet) {
        Set<Specimen> specimens = new HashSet<>();
        Table modelTable = tableSet.get("metadata-model.tsv");

        Set<HostStrain> hostStrains = new HashSet<>();
        Set<EngraftmentSite> engraftmentSites = new HashSet<>();
        Set<EngraftmentType> engraftmentTypes = new HashSet<>();
        Set<EngraftmentMaterial> engraftmentMaterials = new HashSet<>();

        for (Row row : modelTable) {
            String modelId = row.getString(TSV.Metadata.model_id.name());
            String hostStrainName = row.getString(TSV.Metadata.host_strain.name());
            String hostStrainNomenclature = row.getString(TSV.Metadata.host_strain_full.name());
            String engraftmentSiteName = row.getString(TSV.Metadata.engraftment_site.name());
            String engraftmentTypeName = row.getString(TSV.Metadata.engraftment_type.name());
            String sampleType = row.getString(TSV.Metadata.sample_type.name());
            String sampleState = row.getString(TSV.Metadata.sample_state.name());
            String passageNum = row.getString(TSV.Metadata.passage_number.name());

            HostStrain hostStrain = new HostStrain(hostStrainName, hostStrainNomenclature);
            EngraftmentSite engraftmentSite = new EngraftmentSite(engraftmentSiteName);
            EngraftmentType engraftmentType = new EngraftmentType(engraftmentTypeName);
            EngraftmentMaterial engraftmentMaterial = new EngraftmentMaterial(sampleType, sampleState);
            Sample xenoSample = sampleCreator.createHostSampleForSpecimen(modelId, passageNum);

            // externalId not set?
            Specimen s = new Specimen();
            s.setPassage(passageNum);
            s.setHostStrain(hostStrain);
            s.setEngraftmentMaterial(engraftmentMaterial);
            s.setEngraftmentSite(engraftmentSite);
            s.setEngraftmentType(engraftmentType);
            s.setSample(xenoSample);

            specimens.add(s);
            hostStrains.add(hostStrain);
            engraftmentSites.add(engraftmentSite);
            engraftmentTypes.add(engraftmentType);
            engraftmentMaterials.add(engraftmentMaterial);
        }
        return specimens;
    }
}
