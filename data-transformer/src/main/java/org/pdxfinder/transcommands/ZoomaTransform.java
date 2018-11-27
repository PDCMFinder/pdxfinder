package org.pdxfinder.transcommands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.pdxfinder.admin.pojos.MappingEntity;
import org.pdxfinder.admin.zooma.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class ZoomaTransform {



    public static final String URI = "http://www.pdxfinder.org/";
    private static final String NAME = "pdx-finder";
    private static final List<String> TOPIC = Arrays.asList("PDXFinder");
    private static final String TYPE = "DATABASE";
    private static final String EVIDENCE = "SUBMITTER_PROVIDED";
    private static final String ANNOTATOR = "Nathalie Conte";

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    DataTransformerService transformerService;

    public ZoomaTransform() {
    }


    public List<ZoomaEntity> transforMappingsForZooma(String knowledgBaseURL){

        JsonNode mappingRow = transformerService.connectToJSON(knowledgBaseURL);

        Map<String, List<Object>> dMappingRow = mapper.convertValue(mappingRow, Map.class);

        List<ZoomaEntity> zoomaEntities = new ArrayList<>();

        for (Object data : dMappingRow.get("row")) {

            MappingEntity mappingEntity = mapper.convertValue(data, MappingEntity.class);

            /* RETRIEVE DATA FROM MAPPING ENTITY */
            Long entityId = mappingEntity.getEntityId();
            String entityType = mappingEntity.getEntityType();
            List<String> mappingLabels = mappingEntity.getMappingLabels();

            Map<String, String> mappingValues = mappingEntity.getMappingValues();
            String originTissue = mappingValues.get("OriginTissue");
            String tumorType = mappingValues.get("TumorType");
            String sampleDiagnosis = mappingValues.get("SampleDiagnosis");
            String dataSource = mappingValues.get("DataSource");

            String mappedTermLabel = mappingEntity.getMappedTermLabel();
            String mappedTermUrl = mappingEntity.getMappedTermUrl();
            String mapType = mappingEntity.getMapType();
            String justification = mappingEntity.getJustification();
            String status = mappingEntity.getStatus();


            /* ZOOMA BIOLOGICAL-ENTITY DATA */
            Studies studies = new Studies(dataSource.toUpperCase(), null);
            String bioEntity = StringUtils.join(
                    Arrays.asList(dataSource, sampleDiagnosis, originTissue, tumorType), "__"
            );
            BiologicalEntities biologicalEntities = new BiologicalEntities(bioEntity.toUpperCase(),studies,null);

            /* ZOOMA SEMANTIC-TAG DATA */
            List<String> semanticTag = Arrays.asList(mappedTermUrl);

            /* ZOOMA PROVENANCE DATA */
            Source source  = new Source(URI,NAME,TOPIC,TYPE);
            Provenance provenance = new Provenance(
                    source,
                    EVIDENCE,
                    mapType.toUpperCase(),
                    ANNOTATOR,
                    "2018-11-01 10:48"
            );

            for (String mappingLabel : mappingLabels){

                /* ZOOMA PROPERTY DATA */
                Property property = new Property(mappingLabel,StringUtils.upperCase(mappingValues.get(mappingLabel)) );
                ZoomaEntity zoomaEntity = new ZoomaEntity(
                        biologicalEntities,
                        property,
                        semanticTag,
                        provenance
                );
                zoomaEntities.add(zoomaEntity);
            }


        }



        return zoomaEntities;

    }



}
