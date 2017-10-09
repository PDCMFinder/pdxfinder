package org.pdxfinder.services;


import org.pdxfinder.dao.*;
import org.pdxfinder.repositories.*;
import org.pdxfinder.services.dto.DetailsDTO;
import org.pdxfinder.services.dto.SearchDTO;
import org.pdxfinder.services.dto.VariationDataDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SearchService {


    private SampleRepository sampleRepository;

    private PatientRepository patientRepository;
    private PatientSnapshotRepository patientSnapshotRepository;
    private ModelCreationRepository modelCreationRepository;
    private OntologyTermRepository ontologyTermRepositoryRepository;
    private SpecimenRepository specimenRepository;
    private MolecularCharacterizationRepository molecularCharacterizationRepository;
    private PlatformRepository platformRepository;
    private final String JAX_URL = "http://tumor.informatics.jax.org/mtbwi/pdxDetails.do?modelID=";
    private final String JAX_URL_TEXT = "View data at JAX";
    private final String IRCC_URL = "mailto:andrea.bertotti@unito.it?subject=";
    private final String IRCC_URL_TEXT = "Contact IRCC here";


    @Autowired
    public SearchService(SampleRepository sampleRepository, PatientRepository patientRepository,
                         PatientSnapshotRepository patientSnapshotRepository, ModelCreationRepository modelCreationRepository,
                         OntologyTermRepository ontologyTermRepository,SpecimenRepository specimenRepository,
                         MolecularCharacterizationRepository molecularCharacterizationRepository,PlatformRepository platformRepository) {
        this.sampleRepository = sampleRepository;
        this.patientRepository = patientRepository;
        this.patientSnapshotRepository = patientSnapshotRepository;
        this.modelCreationRepository = modelCreationRepository;
        this.ontologyTermRepositoryRepository = ontologyTermRepository;
        this.molecularCharacterizationRepository = molecularCharacterizationRepository;
        this.specimenRepository = specimenRepository;
        this.platformRepository = platformRepository;

    }


    public int modelCount() {

        int pdxCount = modelCreationRepository.countAllModels();
        return  pdxCount;

    }


    public Boolean isExistingOntologyTerm(String query) {

        if (ontologyTermRepositoryRepository.findByLabel(query) != null) return true;
        return false;

    }


    public List<SearchDTO> searchWithOntology(String query, String[] markers, String[] datasources, String[] origintumortypes) {

        Collection<ModelCreation> models = modelCreationRepository.findByOntology(query, markers, datasources, origintumortypes);

        return createSearchResult(models, query);

    }

    public List<SearchDTO> searchForModelsWithFilters(String diag, String[] markers, String[] datasources, String[] origintumortypes) {

        Collection<ModelCreation> models = modelCreationRepository.findByMultipleFilters(diag, markers, datasources, origintumortypes);

        return createSearchResult(models, diag);
    }

    public List<SearchDTO> search(String query, String[] markers, String[] datasources, String[] origintumortypes) {

        if (isExistingOntologyTerm(query)) return searchWithOntology(query, markers, datasources, origintumortypes);

        else return searchForModelsWithFilters(query, markers, datasources, origintumortypes);

    }


    public List<SearchDTO> createSearchResult(Collection<ModelCreation> models, String query) {

        List<SearchDTO> results = new ArrayList<>();

        for (ModelCreation model : models) {

            SearchDTO sdto = new SearchDTO();

            if (model.getSourcePdxId() != null) {
                sdto.setModelId(model.getSourcePdxId());
            }

            if (model.getSample() != null && model.getSample().getDataSource() != null) {
                sdto.setDataSource(model.getSample().getDataSource());
            }

            if (model.getSample() != null && model.getSample().getSourceSampleId() != null) {
                sdto.setTumorId(model.getSample().getSourceSampleId());
            }

            if (model.getSample() != null && model.getSample().getDiagnosis() != null) {
                sdto.setDiagnosis(model.getSample().getDiagnosis());
            }

            if (model.getSample() != null && model.getSample().getOriginTissue() != null) {
                sdto.setTissueOfOrigin(model.getSample().getOriginTissue().getName());
            }

            if (model.getSample() != null && model.getSample().getType() != null) {
                sdto.setTumorType(model.getSample().getType().getName());
            }

            if (model.getSample() != null && model.getSample().getClassification() != null) {
                sdto.setClassification(model.getSample().getClassification());
            }

            sdto.setSearchParameter(query);


            if (model.getSample() != null && model.getSample().getMolecularCharacterizations() != null) {
                Set<String> markerSet = new HashSet<>();

                for (MolecularCharacterization mc : model.getSample().getMolecularCharacterizations()) {
                    for (MarkerAssociation ma : mc.getMarkerAssociations()) {
                        markerSet.add(ma.getMarker().getName());
                    }
                }
                sdto.setCancerGenomics(new ArrayList<>(markerSet));

            }

            results.add(sdto);

        }

        return results;


    }


    public List<Platform> findPlatfromByModelId(String dataSource, String modelId){
        return platformRepository.findByModelId(dataSource,modelId);
    }


    public DetailsDTO searchForModel(String dataSource, String modelId, int page, int size,String technology) {


        Sample sample = sampleRepository.findByDataSourceAndPdxId(dataSource,modelId);
        Patient patient = patientRepository.findByDataSourceAndModelId(dataSource,modelId);
        List<PatientSnapshot> ps = patientSnapshotRepository.findByDataSourceAndModelId(dataSource,modelId);
        ModelCreation pdx = modelCreationRepository.findBySourcePdxId(modelId);

        int skip = page * size;
        int totalRecords = 0;
        Set<Specimen> specimens = new HashSet<>();


        if (technology.equals("")){

            totalRecords = molecularCharacterizationRepository.countBySearchParameter(modelId,"");
            specimens = specimenRepository.findSpecimenBySourcePdxId(dataSource,modelId,"",skip,size);
        }
        else{

            totalRecords = specimenRepository.countBySearchParameterAndPlatform(dataSource,modelId,technology,"");
            specimens = specimenRepository.findSpecimenBySourcePdxIdAndPlatform(dataSource,modelId,technology,"",skip,size);
        }


        DetailsDTO dto = new DetailsDTO();

                        /*
                        this.modelId = "";
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
                        this.sampleSite = "";Å

                        this.sampleType = "";
                        this.strain = "";
                        this.mouseSex = "";
                        this.engraftmentSite = "";
                         */

        Set< Set<MarkerAssociation> > markerAssociatonSet = new HashSet<>();
        List<Specimen> specimenList = new ArrayList<>();
        Set<MolecularCharacterization>  molecularCharacterizations = new HashSet<>();
        Set<Platform>  platforms = new HashSet<>();

        if (specimens != null) {

            try {
                double dSize = size;
                dto.setTotalPages((int) Math.ceil(totalRecords/dSize) );
                dto.setVariationDataCount(totalRecords);
            }catch (Exception e){ }
        }

        for (Specimen specimen : specimens) {
            try
            {
                specimenList.add(specimen);
                molecularCharacterizations.addAll(specimen.getSample().getMolecularCharacterizations());

                for (MolecularCharacterization dMolkar : specimen.getSample().getMolecularCharacterizations()) {
                    markerAssociatonSet.add(dMolkar.getMarkerAssociations());
                    platforms.add(dMolkar.getPlatform());
                }
            }catch (Exception e){ }
        }

        dto.setMarkerAssociations(markerAssociatonSet);
        dto.setMolecularCharacterizations(molecularCharacterizations);
        dto.setPlatforms(platforms);



        try {
            dto.setSpecimens(specimenList);
        }catch (Exception e){ }


        if (sample != null && sample.getSourceSampleId() != null) {
            dto.setExternalId(sample.getSourceSampleId());
        }

        if (sample != null && sample.getDataSource() != null) {
            dto.setDataSource(sample.getDataSource());
        }

        if (patient != null && patient.getExternalId() != null) {
            dto.setPatientId(patient.getExternalId());
        }

        if (patient != null && patient.getSex() != null) {
            dto.setGender(patient.getSex());
        }

        if (ps != null) {
            for (PatientSnapshot patientSnapshots : ps) {
                if (patientSnapshots != null && patientSnapshots.getAge() != null) {
                    dto.setAge(patientSnapshots.getAge());
                }
            }

        }

        if (patient != null && patient.getRace() != null) {
            dto.setRace(patient.getRace());
        }

        if (patient != null && patient.getEthnicity() != null) {
            dto.setEthnicity(patient.getEthnicity());
        }

        if (sample != null && sample.getDiagnosis() != null) {
            dto.setDiagnosis(sample.getDiagnosis());
        }

        if (sample != null && sample.getType() != null) {
            dto.setTumorType(sample.getType().getName());
        }

        if (sample != null && sample.getClassification() != null) {
            dto.setClassification(sample.getClassification());
        }

        if (sample != null && sample.getOriginTissue() != null) {
            dto.setOriginTissue(sample.getOriginTissue().getName());
        }
        if (sample != null && sample.getSampleSite() != null) {
            dto.setSampleSite(sample.getSampleSite().getName());
        }

        if (pdx != null && pdx.getImplantationType() != null) {
            dto.setSampleType(pdx.getImplantationType().getName());
        }

        if (pdx != null && pdx.getBackgroundStrain() != null) {
            dto.setStrain(pdx.getBackgroundStrain().getName());
        }

        if (pdx != null && pdx.getImplantationSite() != null) {
            dto.setEngraftmentSite(pdx.getImplantationSite().getName());
        }

        if (pdx != null && pdx.getSourcePdxId() != null) {
            dto.setModelId(pdx.getSourcePdxId());
        }

        if (sample != null && sample.getMolecularCharacterizations() != null) {
            List<String> markerList = new ArrayList<>();

            for (MolecularCharacterization mc : sample.getMolecularCharacterizations()) {
                for (MarkerAssociation ma : mc.getMarkerAssociations()) {

                    if (ma.getDescription().equals("None")) {
                        markerList.add("None");
                    } else {
                        markerList.add(ma.getMarker().getName() + " status: " + ma.getDescription());
                    }

                }
            }
            Collections.sort(markerList);
            dto.setCancerGenomics(markerList);

        }

        if (sample != null && sample.getDataSource().equals("JAX")) {
            dto.setExternalUrl(JAX_URL+pdx.getSourcePdxId());
            dto.setExternalUrlText(JAX_URL_TEXT);
        } else if (sample != null && sample.getDataSource().equals("IRCC")) {
            dto.setExternalUrl(IRCC_URL + dto.getExternalId());
            dto.setExternalUrlText(IRCC_URL_TEXT);
        }
        else{
            dto.setExternalUrl("#");
            dto.setExternalUrlText("Unknown source");
        }


        return dto;
    }





    public VariationDataDTO variationDataByPlatform(String dataSource, String modelId, String technology,
                                                             String searchParam, int draw, String sortColumn, String sortDir, int start, int size) {
        /**
         * 1st count all the records and set Total Records & Initialize Filtered Record as Total record
         */
        int recordsTotal = specimenRepository.countBySearchParameterAndPlatform(dataSource,modelId,technology,"");
        int recordsFiltered = recordsTotal;

        /**
         * If search Parameter is not empty: Count and Reset the value of filtered records based on search Parameter
         */
        if (!searchParam.isEmpty()) {
            recordsFiltered = specimenRepository.countBySearchParameterAndPlatform(dataSource,modelId,technology,searchParam);
        }

        /**
         * Retrieve the Records based on search parameter
         */
        Set<Specimen> specimen = specimenRepository.findSpecimenBySourcePdxIdAndPlatform(dataSource,modelId,technology,searchParam,start,size);
        VariationDataDTO variationDataDTO = buildUpDTO(specimen,draw,recordsTotal,recordsFiltered);
        return variationDataDTO;

    }





    public VariationDataDTO variationDataAll(String dataSource, String modelId,
                                     String searchParam, int draw, String sortColumn, String sortDir, int start, int size) {
        /**
         * 1st count all the records and set Total Records & Initialize Filtered Record as Total record
         */
        int recordsTotal = molecularCharacterizationRepository.countBySearchParameter(modelId,"");
        int recordsFiltered = recordsTotal;

        /**
         * If search Parameter is not empty: Count and Reset the value of filtered records based on search Parameter
         */
        if (!searchParam.isEmpty()) {
            recordsFiltered = molecularCharacterizationRepository.countBySearchParameter(modelId,searchParam);
        }

        /**
         * Retrieve the Records based on search parameter
         */
        Set<Specimen> specimen = specimenRepository.findSpecimenBySourcePdxId(dataSource,modelId,searchParam,start,size);
        VariationDataDTO variationDataDTO = buildUpDTO(specimen,draw,recordsTotal,recordsFiltered);
        return variationDataDTO;

    }




    public VariationDataDTO buildUpDTO(Set<Specimen> specimens,int draw,int recordsTotal,int recordsFiltered){

        VariationDataDTO variationDataDTO = new VariationDataDTO();
        List<String[]> variationData = new ArrayList();
        String sampleId = "";


        /**
         * Generate an equivalent 2-D array type of the Retrieved result Set, the Front end table must be a 2D JSON Array
         */
        if (specimens != null) {
            for (Specimen specimen : specimens) {

                try {

                        for (MolecularCharacterization dMolChar : specimen.getSample().getMolecularCharacterizations()) {

                            List<MarkerAssociation> markerAssociation = new ArrayList();// = dMolChar.getMarkerAssociations();
                            markerAssociation.addAll(dMolChar.getMarkerAssociations());

                            for (MarkerAssociation markerAssoc : markerAssociation) {

                                String[] markerAssocArray = new String[12];
                                markerAssocArray[0] = specimen.getExternalId();
                                markerAssocArray[1] = dMolChar.getPlatform().getName();
                                markerAssocArray[2] = markerAssoc.getChromosome();
                                markerAssocArray[3] = markerAssoc.getSeqPosition();
                                markerAssocArray[4] = markerAssoc.getRefAllele();
                                markerAssocArray[5] = markerAssoc.getAltAllele();
                                markerAssocArray[6] = markerAssoc.getConsequence();
                                markerAssocArray[7] = markerAssoc.getMarker().getSymbol();
                                markerAssocArray[8] = markerAssoc.getAminoAcidChange();
                                markerAssocArray[9] = markerAssoc.getReadDepth();
                                markerAssocArray[10] = markerAssoc.getAlleleFrequency();
                                markerAssocArray[11] = markerAssoc.getRsVariants();

                                variationData.add(markerAssocArray);
                            }

                        }
                    }catch (Exception e) { }

            }/* End of Specimens Set Loop */

        } /* End of Specimens check */


        /**
         * Transfer the data to the DTO
         */
        variationDataDTO.setDraw(draw);
        variationDataDTO.setRecordsTotal(recordsTotal);
        variationDataDTO.setRecordsFiltered(recordsFiltered);
        variationDataDTO.setData(variationData);

        return variationDataDTO;
    }







}
