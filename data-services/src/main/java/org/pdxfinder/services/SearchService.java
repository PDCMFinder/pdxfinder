package org.pdxfinder.services;

import org.pdxfinder.dao.BackgroundStrain;
import org.pdxfinder.dao.Marker;
import org.pdxfinder.dao.Sample;
import org.pdxfinder.repositories.SampleRepository;
import org.pdxfinder.services.dto.SearchDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Service
public class SearchService {


    private SampleRepository sampleRepository;

    private MarkerService markerService;


    @Autowired
    public SearchService(SampleRepository sampleRepository, MarkerService markerService) {
        this.sampleRepository = sampleRepository;
        this.markerService = markerService;
    }

       public List<SearchDTO> searchForSamplesWithFilters(String diag, String[] markers, String[] datasources, String[] origintumortypes){

        if(markers.length == 0){
            List<String> markerList = markerService.getAllMarkers();


        }

        Collection<Sample> samples = sampleRepository.findByDiagnosisContainsAndHaveMarkers(diag, markers);
        List<SearchDTO> results = new ArrayList<>();

        for (Sample sample : samples) {

            SearchDTO sdto = new SearchDTO();

            sdto.setDataSource(sample.getDataSource());
            sdto.setTumorId(sample.getSourceSampleId());
            sdto.setDiagnosis(sample.getDiagnosis());
            sdto.setTissueOfOrigin(sample.getOriginTissue().getName());
            sdto.setTumorType(sample.getType().getName());
            sdto.setClassification(sample.getClassification());
            sdto.setCancerGenomics(markerService.getAllMarkerNamesBySampleId(sample.getSourceSampleId()));

            results.add(sdto);
        }

        return results;

    }





}
