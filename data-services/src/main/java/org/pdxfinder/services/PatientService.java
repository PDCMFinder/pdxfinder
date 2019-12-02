package org.pdxfinder.services;

import org.pdxfinder.graph.dao.*;
import org.pdxfinder.graph.repositories.PatientRepository;
import org.pdxfinder.services.dto.CollectionEventsDTO;
import org.pdxfinder.services.dto.PatientDTO;
import org.pdxfinder.services.dto.TreatmentSummaryDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/*
 * Created by abayomi on 13/09/2018.
 */
@Service
public class PatientService {

    private final static Logger logger = LoggerFactory.getLogger(PatientService.class);

    private PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public PatientDTO getPatientDetails(String dataSource, String modelId) {

        Patient patient = patientRepository.findByPatientByModelId(dataSource,modelId);
        PatientDTO patientDTO =  new PatientDTO();

        List<CollectionEventsDTO> collectionEvents = new ArrayList<>();
        Boolean treatmentExists = false;
        Boolean currentTreatmentExists = false;


        if (patient != null){

            patientDTO.setGender(patient.getSex());
            patientDTO.setAgeAtDiagnosis(patient.getAgeAtFirstDiagnosis());
            patientDTO.setDiseaseBodyLocation("Not Available");
            patientDTO.setCTEPSDCCode("Not Available");
            patientDTO.setDiagnosisSubtype("Not Available");
            patientDTO.setRaceAndEthnicity(patient.getRace()+"/"+patient.getEthnicity());

            List<String> geneticMutations = new ArrayList<>();
            List<TreatmentSummaryDTO> treatmentSummaries = new ArrayList<>();

            Set<PatientSnapshot> patientSnapshots = patient.getSnapshots();

            for (PatientSnapshot ps : patientSnapshots) {


                for (Sample sample : ps.getSamples()){

                    String age = "Not Specified";
                    String diagnosis = "Not Specified";
                    String tumorType = "Not Specified";
                    String pdxMouse = "Not Specified";
                    String data = "Not Specified";
                    String collectionSite = "Not Specified";

                    try {
                        age = ps.getAgeAtCollection();
                    } catch (Exception e) {}

                    try {
                        diagnosis = sample.getSampleToOntologyRelationship().getOntologyTerm().getLabel();
                    } catch (Exception e) {}

                    try {
                        tumorType = sample.getType().getName();
                    } catch (Exception e) {}

                    try {
                        pdxMouse = patientRepository.getModelIdByDataSourceAndPatientSampleId(dataSource, sample.getSourceSampleId());
                    } catch (Exception e) {}

                    try {
                        collectionSite = notEmpty(sample.getSampleSite().getName());
                    } catch (Exception e) {}


                    try{
                        for (MolecularCharacterization molc : sample.getMolecularCharacterizations()){
                            for (MarkerAssociation mAssoc : molc.getMarkerAssociations() ){
                                List<MolecularData> maData = mAssoc.getMolecularDataList();
                                for(MolecularData md: maData){
                                    geneticMutations.add( md.getMarker() );
                                }

                            }
                        }
                    }catch (Exception e) {}


                    collectionEvents.add(new CollectionEventsDTO(age, diagnosis, tumorType, pdxMouse, data, collectionSite));
                }



                String mappedTreatmentName = "";
                String treatmentDose = "-";
                String treatmentResponse = "";
                String treatmentDuration = "";
                String treatmentDate = "";

                boolean current = false;
                try{

                    // Aggregate the treatment summaries for this Treatment Protocol
                    for (TreatmentProtocol protocol : ps.getTreatmentSummary().getTreatmentProtocols()){

                        mappedTreatmentName = protocol.getTreatmentString(false);
                        treatmentResponse = protocol.getResponse().getDescription();
                        treatmentDose = notEmpty(protocol.getDoseString(false));
                        treatmentDuration = protocol.getDurationString(false);
                        treatmentDate = protocol.getTreatmentDate();

                        if (protocol.getCurrentTreatment() != null){
                            current = true;
                            currentTreatmentExists = true;
                        }else{
                            current = false;
                        }

                        treatmentSummaries.add(new TreatmentSummaryDTO(treatmentDate,mappedTreatmentName, treatmentDose, treatmentResponse, treatmentDuration, current));
                        treatmentExists = true;
                    }

                }catch (Exception e){}

            }

            patientDTO.setKnownGeneticMutations(geneticMutations);
            patientDTO.setCollectionEvents(collectionEvents);
            patientDTO.setTreatmentSummaries(treatmentSummaries);
            patientDTO.setTreatmentExists(treatmentExists);
            patientDTO.setCurrentTreatmentExists(currentTreatmentExists);



        }

        return patientDTO;

    }


    public String notEmpty(String input){

        String output = (input == null) ? "Not Specified" : input;
        output = output.equals("null") ? "Not Specified" : output;
        output = output.length() == 0 ? "Not Specified" : output;
        output = output.equals("Unknown") ? "Not Specified" : output;

        return output;
    }

}
