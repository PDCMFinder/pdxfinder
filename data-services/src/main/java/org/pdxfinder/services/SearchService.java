package org.pdxfinder.services;



import org.pdxfinder.dao.*;
import org.pdxfinder.repositories.*;
import org.pdxfinder.services.dto.DetailsDTO;
import org.pdxfinder.services.dto.SearchDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SearchService {


    private SampleRepository sampleRepository;

    private PatientRepository patientRepository;
    private PatientSnapshotRepository patientSnapshotRepository;
    private PdxStrainRepository pdxStrainRepository;


    @Autowired
    public SearchService(SampleRepository sampleRepository, PatientRepository patientRepository,
                         PatientSnapshotRepository patientSnapshotRepository, PdxStrainRepository pdxStrainRepository) {
        this.sampleRepository = sampleRepository;
        this.patientRepository = patientRepository;
        this.patientSnapshotRepository = patientSnapshotRepository;
        this.pdxStrainRepository = pdxStrainRepository;

    }

    public List<SearchDTO> searchForSamplesWithFilters(String diag, String[] markers, String[] datasources, String[] origintumortypes){

        Collection<Sample> samples = sampleRepository.findByMultipleFilters(diag, markers, datasources, origintumortypes);

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

            if(sample.getOriginTissue() != null){
                sdto.setTissueOfOrigin(sample.getOriginTissue().getName());
            }

            if(sample.getType() != null){
                sdto.setTumorType(sample.getType().getName());
            }

            if(sample.getClassification() != null) {
                sdto.setClassification(sample.getClassification());
            }
            if(sample.getMolecularCharacterizations() != null){
                Set<String> markerSet = new HashSet<>();

                for(MolecularCharacterization mc : sample.getMolecularCharacterizations()){
                    for(MarkerAssociation ma : mc.getMarkerAssociations()){
                        markerSet.add(ma.getMarker().getName());
                    }
                }
                sdto.setCancerGenomics(new ArrayList<>(markerSet));

            }

            results.add(sdto);
        }

        return results;
    }



    public DetailsDTO searchForSample(String sampleId){

        Sample sample = sampleRepository.findBySampleSourceId(sampleId);
        Patient patient = patientRepository.findBySampleId(sampleId);
        PatientSnapshot ps = patientSnapshotRepository.findBySampleId(sampleId);
        PdxStrain pdx = pdxStrainRepository.findBySampleSourceSampleId(sampleId);

        DetailsDTO dto = new DetailsDTO();

        /*

        this.externalId = "";
        this.dataSource = "";
        this.patientId = "";
        this.gender = "";
        this.age = "";
        this.race = "";
        this.ethnicity = "";
        this.diagnosis = "";
        this.tumorType = "";
        this.classification = "";
        this.originTissue = "";
        this.sampleSite = "";

        this.sampleType = "";
        this.strain = "";
        this.mouseSex = "";
        this.engraftmentSite = "";
         */


        if(sample.getSourceSampleId() != null) {
            dto.setExternalId(sample.getSourceSampleId());
        }

        if(sample.getDataSource() != null) {
                dto.setDataSource(sample.getDataSource());
        }

        if(patient.getExternalId() != null) {
            dto.setPatientId(patient.getExternalId());
        }

        if(patient.getSex() != null) {
            dto.setGender(patient.getSex());
        }

        if(ps.getAge() != null) {
            dto.setAge(ps.getAge());
        }

        if(patient.getRace() != null) {
            dto.setRace(patient.getRace());
        }

        if(patient.getEthnicity() != null) {
            dto.setEthnicity(patient.getEthnicity());
        }

        if(sample.getDiagnosis() != null) {
            dto.setDiagnosis(sample.getDiagnosis());
        }

        if(sample.getType() != null){
            dto.setTumorType(sample.getType().getName());
        }

        if(sample.getClassification() != null) {
            dto.setClassification(sample.getClassification());
        }

        if(sample.getOriginTissue() != null) {
            dto.setOriginTissue(sample.getOriginTissue().getName());
        }
        if(sample.getSampleSite() != null) {
            dto.setSampleSite(sample.getSampleSite().getName());
        }

        if(pdx != null && pdx.getImplantationType() != null){
            dto.setSampleType(pdx.getImplantationType().getName());
        }

        if(pdx != null && pdx.getBackgroundStrain() != null){
            dto.setStrain(pdx.getBackgroundStrain().getName());
        }

        if(pdx != null && pdx.getImplantationSite() != null){
            dto.setEngraftmentSite(pdx.getImplantationSite().getName());
        }


        if(sample.getMolecularCharacterizations() != null){
            List<String> markerList = new ArrayList<>();

            for(MolecularCharacterization mc : sample.getMolecularCharacterizations()){
                for(MarkerAssociation ma : mc.getMarkerAssociations()){

                    if(ma.getDescription().equals("None")){
                        markerList.add("None");
                    }
                    else{
                        markerList.add(ma.getMarker().getName() +" "+ma.getDescription());
                    }

                }
            }
            dto.setCancerGenomics(markerList);

        }

        return dto;
    }



}
