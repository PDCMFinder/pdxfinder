package org.pdxfinder.services;

import org.apache.commons.lang3.StringUtils;
import org.pdxfinder.services.constants.DataUrl;
import org.pdxfinder.services.dto.pdxgun.ApiData;
import org.pdxfinder.services.dto.pdxgun.Reference;
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
import java.util.stream.Stream;

@Service
public class ReferenceDbService {

    private static final String COSM_PREFIX = "COSM";
    private static final String COSMIC = "COSMIC";
    private static final String CRAVAT = "CRAVAT";
    private Logger log = LoggerFactory.getLogger(ReferenceDbService.class);
    private RestTemplate restTemplate;

    public ReferenceDbService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @SuppressWarnings("WeakerAccess")
    public Map<String, Reference> getReferenceData(List<String> markerList, String dataType){

        Map<String, Condition> conditions = new LinkedHashMap<>();
        conditions.put("name", new Condition().setOperator(Operator.IN).setValue(markerList));

        Select select = new Select()
                .columns(Arrays.asList("name", "url", "resource{name}"))
                .table(dataType)
                .conditions(conditions);
        String graphQlQuery = GraphQlBuilder.selectQuery(select);
        HttpEntity<Object> request = new HttpEntity<>(graphQlQuery);

        ReferenceData referenceData = new ReferenceData();
        Map<String, Reference> markerDataMap = new HashMap<>();
        try{
            referenceData = restTemplate.postForObject(DataUrl.K8_SERVICE_URL.get(), request, ReferenceData.class);
            markerDataMap = clusterReferenceDataByMarker(referenceData, dataType);
        }catch (Exception e){
            log.info("Reference Database could not be retrieved");
        }
        return  markerDataMap;
    }

    private Map<String, Reference> clusterReferenceDataByMarker(ReferenceData referenceData, String dataType){

        Map<String, Map<String, String>> dataMap = new HashMap<>();

        List<ApiData> dData;
        if (dataType.equals("gene")){
            dData = referenceData.getData().getGene();
        }else {
            dData = referenceData.getData().getVariant();
        }
        dData.forEach(data -> {
            String dataName = data.getName();
            String resource = data.getResource().getName();
            String url = data.getUrl();

            if (dataMap.containsKey(dataName)){
                dataMap.get(dataName).put(resource, url);
            }else {
                Map<String, String> map = new HashMap<>();
                map.put(resource, url);
                dataMap.put(data.getName(), map);
            }
        });

        Map<String, Reference> markerDataMap = new HashMap<>();
        dataMap.forEach((key, value)->{
            Reference reference = new Reference();
            reference.setLabel(key)
                    .setReferenceDbs(value)
                    .build();
            markerDataMap.put(key, reference);
        });
        return markerDataMap;
    }

    @SuppressWarnings("WeakerAccess")
    public Reference getAminoAcidChangeReference(String aminoAcidChange,
                                                 Map<String, Reference> referenceDataMap,
                                                 String variation,
                                                 String chrom,
                                                 String seqStart,
                                                 String refAllele,
                                                 String altAllele){

        Map<String, String> referenceDBs = new HashMap<>();
        Reference reference = null;

        if (Optional.ofNullable(aminoAcidChange).isPresent()){
            reference = getReference(aminoAcidChange, referenceDataMap);
            referenceDBs = Optional.ofNullable(reference.getReferenceDbs()).orElse(new HashMap<>());
        }

        if (Optional.ofNullable(variation).isPresent()){
            if (variation.contains(COSM_PREFIX)){
                String cosmValue = variation.split(COSM_PREFIX)[1].replace(",","");
                referenceDBs.put(COSMIC, String.format("%s=%s", DataUrl.COSMIC_URL.get(), cosmValue));
            }

            if (StringUtils.containsIgnoreCase(variation, "Rs") &&
                    Stream.of(chrom, seqStart, refAllele, altAllele).allMatch(Objects::nonNull)) {
                referenceDBs.put(CRAVAT, String.format("%s?chrom=chr%s&pos=%s&ref_base=%s&alt_base=%s",
                                                       DataUrl.CRAVAT_URL.get(),
                                                       chrom.replace("chr",""),
                                                       seqStart,
                                                       refAllele,
                                                       altAllele));
            }
            return new Reference(aminoAcidChange).setReferenceDbs(referenceDBs);
        }
        return reference;
    }

    public Reference getReference(String symbol, Map<String, Reference> referenceDataMap){
        Optional<Reference> optionalMarkerData = Optional.ofNullable(referenceDataMap.get(symbol));
        return optionalMarkerData.orElse(new Reference(symbol));
    }

}
