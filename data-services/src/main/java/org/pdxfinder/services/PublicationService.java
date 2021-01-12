package org.pdxfinder.services;

import org.pdxfinder.services.constants.DataUrl;
import org.pdxfinder.services.dto.europepmc.Publication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class PublicationService {

    private RestTemplate restTemplate;

    private static final String PUBLICATION_PREFIX = "PMID:";
    private static final String EMPTY_STRING = "";

    public PublicationService(RestTemplate restTemplate) {
      this.restTemplate = restTemplate;
    }

    @SuppressWarnings("WeakerAccess")
    public List<Publication> getEuropePmcPublications(List<String> pubMedIds) {
        pubMedIds = sanitizePubMedIds(pubMedIds);
        List<Publication> publications = new ArrayList<>();
        pubMedIds.forEach(pubMedId->{
            String api = String.format("%s?query=ext_id:%s&resultType=core&format=json", DataUrl.EUROPE_PMC_URL.get(), pubMedId);
            publications.add(restTemplate.getForObject(api, Publication.class));
        });

        return publications;
    }


    @SuppressWarnings("WeakerAccess")
    public List<String> sanitizePubMedIds(List<String> pubMedIds){
        List<String> cleanedPubMedIds = new ArrayList<>();
        pubMedIds.forEach(pubMedId -> {
            pubMedId = pubMedId.toUpperCase();
            pubMedId = pubMedId.replace(PUBLICATION_PREFIX, EMPTY_STRING);
            pubMedId = pubMedId.replaceAll("\\s+",EMPTY_STRING);
            if (pubMedId.contains(";")){
                cleanedPubMedIds.addAll(Arrays.asList(pubMedId.split(";")));
            }else {
                cleanedPubMedIds.add(pubMedId);
            }
        });
        return cleanedPubMedIds;
    }

}
