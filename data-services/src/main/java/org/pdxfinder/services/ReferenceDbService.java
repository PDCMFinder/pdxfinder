package org.pdxfinder.services;

import org.pdxfinder.graph.dao.MolecularData;
import org.pdxfinder.services.constants.DataUrl;
import org.pdxfinder.services.dto.pdxgun.MarkerData;
import org.pdxfinder.services.dto.pdxgun.ReferenceData;
import org.pdxfinder.services.graphqlclient.Condition;
import org.pdxfinder.services.graphqlclient.GraphQlBuilder;
import org.pdxfinder.services.graphqlclient.Operator;
import org.pdxfinder.services.graphqlclient.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class ReferenceDbService {

    private Logger log = LoggerFactory.getLogger(ReferenceDbService.class);
    private RestTemplate restTemplate;

    public ReferenceDbService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @SuppressWarnings("WeakerAccess")
    public List<String> getMarkerListFromMolecularData(List<MolecularData> molecularDataList){

        List<String> markerList = new ArrayList<>();
        molecularDataList.forEach( mData -> markerList.add(mData.getMarker()) );

        return markerList;
    }

    @SuppressWarnings("WeakerAccess")
    public Map<String, MarkerData> getReferenceDataForMarkerList(List<String> markerList){

        Map<String, Condition> conditions = new LinkedHashMap<>();
        conditions.put("name", new Condition().setOperator(Operator.IN).setValue(markerList));

        Select select = new Select()
                .columns(Arrays.asList("name", "url", "resource{name}"))
                .table("gene")
                .conditions(conditions);
        String graphQlQuery = GraphQlBuilder.selectQuery(select);
        HttpEntity<Object> request = new HttpEntity<>(graphQlQuery);

        ReferenceData referenceData = new ReferenceData();
        Map<String, MarkerData> markerDataMap = new HashMap<>();
        try{
            referenceData = restTemplate.postForObject(DataUrl.K8_SERVICE_URL.get(), request, ReferenceData.class);
            markerDataMap = clusterReferenceDataByMarker(referenceData);
        }catch (Exception e){
            log.info("Reference Database could not be retrieved {}", e);
        }
        return  markerDataMap;
    }



    private Map<String, MarkerData> clusterReferenceDataByMarker(ReferenceData referenceData){

        Map<String, Map<String, String>> dataMap = new HashMap<>();
        referenceData.getData().getGene().forEach(gene -> {

            String geneName = gene.getName();
            String resource = gene.getResource().getName();
            String url = gene.getUrl();

            if (dataMap.containsKey(geneName)){
                dataMap.get(geneName).put(resource, url);
            }else {
                Map<String, String> map = new HashMap<>();
                map.put(resource, url);
                dataMap.put(gene.getName(), map);
            }
        });

        Map<String, MarkerData> markerDataMap = new HashMap<>();
        dataMap.forEach((key, value)->{
            MarkerData markerData = new MarkerData();
            markerData.setSymbol(key)
                    .setRefData(value)
                    .build();
            markerDataMap.put(key, markerData);
        });

        return markerDataMap;
    }
}
