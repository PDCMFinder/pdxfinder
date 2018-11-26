package org.pdxfinder.transcommands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.pdxfinder.admin.pojos.MappingEntity;
import org.pdxfinder.admin.zooma.Studies;
import org.pdxfinder.admin.zooma.ZoomaEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.jvm.hotspot.debugger.cdbg.LoadObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ZoomaTransform {


    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    DataTransformerService transformerService;

    public ZoomaTransform() {
    }


    public List<ZoomaEntity> transforMappingsForZooma(String knowledgBaseURL){

        List<ZoomaEntity> zoomaEntities = new ArrayList<>();

        Map<String, List<Map>> dMappingRow = mapper.convertValue(transformerService.connectToJSON(knowledgBaseURL), Map.class);

        List<Map> data = dMappingRow.get("row");

        for (Map dMapping : data){

            MappingEntity mapping = mapper.convertValue(dMapping, MappingEntity.class);

            Map<String,String> mappingValues = mapping.getMappingValues();

            String originTissue         = mappingValues.get("OriginTissue");
            String tumorType            = mappingValues.get("TumorType");
            String sampleDiagnosis      = mappingValues.get("SampleDiagnosis");
            String dataSource           = mappingValues.get("DataSource");
            String entityId             = mappingValues.get("entityId");

            String mappedTermLabel           = mappingValues.get("mappedTermLabel");
            String mappedTermUrl           = mappingValues.get("mappedTermUrl");

            ZoomaEntity zoomaEntity = new ZoomaEntity();

            Studies studies = new Studies(dataSource,null);

        }


        return zoomaEntities;

    }



}
