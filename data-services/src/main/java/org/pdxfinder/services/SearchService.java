package org.pdxfinder.services;



import org.pdxfinder.dao.Sample;
import org.pdxfinder.repositories.SampleRepository;
import org.pdxfinder.services.dto.SearchDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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


        Collection<Sample> samples = sampleRepository.findByDiagnosisContainsAndHaveMarkers(diag, markers);
        List<SearchDTO> results = new ArrayList<>();

        for (Sample sample : samples) {

            SearchDTO sdto = new SearchDTO();

            if(sample.getDataSource() != null){
                sdto.setDataSource(sample.getDataSource());
            }

            if(sample.getSourceSampleId() != null){
                sdto.setTumorId(sample.getSourceSampleId());
            }

            if(sample.getDiagnosis() != null){
                sdto.setDiagnosis(sample.getDiagnosis());
            }

            if(sample.getOriginTissue().getName() != null){
                sdto.setTissueOfOrigin(sample.getOriginTissue().getName());
            }

            if(sample.getType().getName() != null){
                sdto.setTumorType(sample.getType().getName());
            }

            if(sample.getClassification() != null) {
                sdto.setClassification(sample.getClassification());
            }
            //sdto.setCancerGenomics(markerService.getAllMarkerNamesBySampleId(sample.getSourceSampleId()));

            results.add(sdto);
        }

        return results;

    }





}
