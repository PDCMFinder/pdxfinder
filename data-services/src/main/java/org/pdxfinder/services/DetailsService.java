package org.pdxfinder.services;

import org.apache.commons.lang3.StringUtils;
import org.pdxfinder.dao.*;
import org.pdxfinder.repositories.*;
import org.pdxfinder.services.dto.DetailsDTO;
import org.pdxfinder.services.dto.DrugSummaryDTO;
import org.pdxfinder.services.dto.VariationDataDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/*
 * Created by abayomi on 09/05/2018.
 */
@Service
public class DetailsService {


    private SampleRepository sampleRepository;

    private PatientRepository patientRepository;
    private PatientSnapshotRepository patientSnapshotRepository;
    private ModelCreationRepository modelCreationRepository;
    private SpecimenRepository specimenRepository;
    private MolecularCharacterizationRepository molecularCharacterizationRepository;
    private PlatformRepository platformRepository;
    private TreatmentSummaryRepository treatmentSummaryRepository;

    private GraphService graphService;
    private Map<String, List<String>> facets = new HashMap<>();
    private PlatformService platformService;
    private DrugService drugService;

    private final String JAX_URL = "http://tumor.informatics.jax.org/mtbwi/pdxDetails.do?modelID=";
    private final String JAX_URL_TEXT = "View data at JAX";
    private final String IRCC_URL = "mailto:andrea.bertotti@unito.it?subject=";
    private final String IRCC_URL_TEXT = "Contact IRCC here";
    private final String PDMR_URL = "https://pdmdb.cancer.gov/pls/apex/f?p=101:41";
    private final String PDMR_URL_TEXT = "View data at PDMR";

    //private final String HCI_URL = "https://www.pdxnetwork.org/hcibcm/";
    private final String HCI_URL = "";
    private final String HCI_DS = "PDXNet-HCI-BCM";
    //    private final String WISTAR_URL = "https://www.pdxnetwork.org/the-wistarmd-andersonpenn/";
    private final String WISTAR_URL = "";

    private final String WISTAR_DS = "PDXNet-Wistar-MDAnderson-Penn";
    private final String MDA_URL = "";
    //private final String MDA_URL = "https://www.pdxnetwork.org/md-anderson/";
    private final String MDA_DS = "PDXNet-MDAnderson";
    //private final String WUSTL_URL = "https://www.pdxnetwork.org/wustl/";
    private final String WUSTL_URL = "";
    private final String WUSTL_DS = "PDXNet-WUSTL";



    public DetailsService(SampleRepository sampleRepository,
                         PatientRepository patientRepository,
                         PatientSnapshotRepository patientSnapshotRepository,
                         ModelCreationRepository modelCreationRepository,
                         SpecimenRepository specimenRepository,
                         MolecularCharacterizationRepository molecularCharacterizationRepository,
                         PlatformRepository platformRepository,
                         TreatmentSummaryRepository treatmentSummaryRepository,
                         GraphService graphService,
                         PlatformService platformService,
                         DrugService drugService) {

        this.sampleRepository = sampleRepository;
        this.patientRepository = patientRepository;
        this.patientSnapshotRepository = patientSnapshotRepository;
        this.modelCreationRepository = modelCreationRepository;
        this.molecularCharacterizationRepository = molecularCharacterizationRepository;
        this.specimenRepository = specimenRepository;
        this.platformRepository = platformRepository;
        this.treatmentSummaryRepository = treatmentSummaryRepository;
        this.graphService = graphService;
        this.platformService = platformService;
        this.drugService = drugService;

    }



    public DetailsDTO getModelDetails(String dataSrc,
                                      String modelId,
                                      Integer page,
                                      Integer size) {


        int viewPage = (page == null || page < 1) ? 0 : page - 1;
        int viewSize = (size == null || size < 1) ? 15000 : size;

        DetailsDTO detailsDTO = searchForModel(dataSrc, modelId, viewPage, viewSize, "", "", "");

        return detailsDTO;


    }


    public DetailsDTO searchForModel(String dataSource, String modelId, int page, int size, String technology, String passage, String searchFilter) {


        Sample sample = sampleRepository.findByDataSourceAndPdxId(dataSource,modelId);
        Patient patient = patientRepository.findByDataSourceAndModelId(dataSource,modelId);
        List<PatientSnapshot> ps = patientSnapshotRepository.findByDataSourceAndModelId(dataSource,modelId);
        ModelCreation pdx = modelCreationRepository.findByDataSourceAndSourcePdxId(dataSource, modelId);

        List<QualityAssurance> qa = pdx.getQualityAssurance();

        int start = page;
        Pageable pageable = new PageRequest(start,size);
        Page<Specimen> specimens = null;
        int totalRecords = 0;


        totalRecords = specimenRepository.countBySearchParameterAndPlatform(dataSource,modelId,technology,passage,searchFilter);

        specimens = specimenRepository.findSpecimenBySourcePdxIdAndPlatform(dataSource,modelId,technology,passage,searchFilter,pageable);


        DetailsDTO dto = new DetailsDTO();

        Set< List<MarkerAssociation> > markerAssociatonSet = new HashSet<>();
        List<Specimen> specimenList = new ArrayList<>();
        Set<MolecularCharacterization>  molecularCharacterizations = new HashSet<>();
        Set<Platform>  platforms = new HashSet<>();

        dto.setQualityAssurances(qa);

        if (specimens != null) {

            try {
                double dSize = size;
                dto.setTotalPages((int) Math.ceil(totalRecords/dSize) );
                dto.setVariationDataCount(totalRecords);
            }catch (Exception e){ }
        }
        dto.setPresentPage(page+1);

        // "NOD SCID GAMA"=>"P1, P2"
        Map<String, String> hostStrainMap = new HashMap<>();

        for (Specimen specimen : specimens) {
            try {
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

        if (patient != null && patient.getExternalDataSource() != null) {
            dto.setContacts(patient.getExternalDataSource().getContact());
            dto.setExternalDataSourceDesc(patient.getExternalDataSource().getDescription());
        }

        if (ps != null) {
            for (PatientSnapshot patientSnapshots : ps) {
                if (patientSnapshots != null && patientSnapshots.getAgeAtCollection() != null) {
                    dto.setAgeAtCollection(patientSnapshots.getAgeAtCollection());
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

        if (sample != null && sample.getSampleToOntologyRelationShip() != null) {
            dto.setMappedOntology(sample.getSampleToOntologyRelationShip().getOntologyTerm().getLabel());
        }


        if (pdx != null && pdx.getSourcePdxId() != null) {
            dto.setModelId(pdx.getSourcePdxId());
        }

        if(pdx!= null && pdx.getSpecimens() != null){

            Set<Specimen> sp = pdx.getSpecimens();

            for(Specimen s:sp){

                if (s.getHostStrain() != null) {

                    String hostStrain = s.getHostStrain().getName();
                    String p = s.getPassage();

                    // If the passage information is provided, associate it to the host strain
                    if (p != null) {
                        if (hostStrainMap.containsKey(hostStrain)) {
                            String composedPassage = hostStrainMap.get(hostStrain);
                            composedPassage += ", P" + p;
                            hostStrainMap.put(hostStrain, composedPassage);

                        } else {
                            hostStrainMap.put(hostStrain, "P" + p);
                        }
                    } else {
                        hostStrainMap.put(hostStrain, "Not Specified");
                    }


                    //Set implantation site and type
                    if(s.getEngraftmentSite() != null){
                        dto.setEngraftmentSite( notEmpty(s.getEngraftmentSite().getName()) );
                    }
                    else{

                        dto.setEngraftmentSite("Not Specified");
                    }

                    if(s.getEngraftmentType() != null){
                        dto.setSampleType( notEmpty(s.getEngraftmentType().getName()) );
                    }
                    else{

                        dto.setSampleType("Not Specified");
                    }



                }

            }


        }

        String composedStrain = "";

        if(hostStrainMap.size() > 1){

            composedStrain = hostStrainMap
                    .keySet()
                    .stream()
                    .map(x -> getHostStrainString(x, hostStrainMap))
                    .collect(Collectors.joining("; "));
        }
        else if(hostStrainMap.size() == 1){

            for(String key:hostStrainMap.keySet()){
                composedStrain = key;
            }
        }
        else{
            composedStrain = "Not Specified";
        }
        dto.setStrain(notEmpty(composedStrain));


        if (sample != null && sample.getMolecularCharacterizations() != null) {
            List<String> markerList = new ArrayList<>();

            for (MolecularCharacterization mc : sample.getMolecularCharacterizations()) {
                for (MarkerAssociation ma : mc.getMarkerAssociations()) {

                   /* if (ma.getDescription().equals("None")) {
                        markerList.add("None");
                    } else {
                        markerList.add(ma.getMarker().getName() + " status: " + ma.getDescription());
                    }*/

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
        } else if(sample != null && sample.getDataSource().equals(HCI_DS)) {
            dto.setExternalUrl(HCI_URL);
            dto.setExternalUrlText(HCI_DS);
        } else if(sample != null && sample.getDataSource().equals(MDA_DS)) {
            dto.setExternalUrl(MDA_URL);
            dto.setExternalUrlText(MDA_DS);
        } else if(sample != null && sample.getDataSource().equals(WUSTL_DS)) {
            dto.setExternalUrl(WUSTL_URL);
            dto.setExternalUrlText(WUSTL_DS);
        } else if(sample != null && sample.getDataSource().equals(WISTAR_DS)) {
            dto.setExternalUrl(WISTAR_URL);
            dto.setExternalUrlText(WISTAR_DS);
        }
        else if(sample != null && sample.getDataSource().equals("PDMR")) {
            dto.setExternalUrl(PDMR_URL);
            dto.setExternalUrlText(PDMR_URL_TEXT);
        }
        else{
            dto.setExternalUrl("#");
            dto.setExternalUrlText("Unknown source");
        }






        Map<String, String> patientTech = findPatientPlatforms(dataSource, modelId);
        dto.setPatientTech(patientTech);

        Map<String, Set<String>> modelTechAndPassages = findModelPlatformAndPassages(dataSource, modelId, "");
        dto.setModelTechAndPassages(modelTechAndPassages);

        List<String> relatedModels = getModelsOriginatedFromSamePatient(dataSource, modelId);
        dto.setRelatedModels(relatedModels);

        List<DrugSummaryDTO> drugSummary = getDrugSummary(dataSource, modelId);
        dto.setDrugSummary(drugSummary);
        dto.setDrugSummaryRowNumber(drugSummary.size());

        String drugProtocolUrl = drugService.getPlatformUrlByDataSource(dataSource);
        dto.setDrugProtocolUrl(drugProtocolUrl);

        List<VariationDataDTO> variationDataDTOList = new ArrayList<>();
        dto.setVariationDataDTOList(variationDataDTOList);

        Map<String, String> techNPassToSampleId = new HashMap<>();
        for (String tech : modelTechAndPassages.keySet()) {

            //Retrieve the passages:
            Set<String> passages = modelTechAndPassages.get(tech);

            // Retrieve variation data by technology and passage
            for (String dPassage : passages) {
                VariationDataDTO variationDataDTO = variationDataByPlatform(dataSource, modelId, tech, dPassage, page, size, "", 1, "mAss.seqPosition", "");
                variationDataDTOList.add(variationDataDTO);

                // Aggregate sampleIds for this Technology and passage in a Set<String>, to remove duplicates
                Set<String> sampleIDSet = new HashSet<>();
                for (String[] data : variationDataDTO.getData()) {
                    sampleIDSet.add(data[0]);
                }

                // Turn the Set<String> to a comma seperated String
                String sampleIDs = "";
                for (String sampleID : sampleIDSet) {
                    sampleIDs += sampleID + ",";
                }

                // Create a Key Value map of (Technology+Passage , sampleIDs) and Pass to DTO
                techNPassToSampleId.put(tech + dPassage, StringUtils.stripEnd(sampleIDs, ","));
            }

        }
        dto.setTechNPassToSampleId(techNPassToSampleId);


        Set<String> autoSuggestList = graphService.getMappedNCITTerms();
        dto.setAutoSuggestList(autoSuggestList);

        Map<String, String> platformsAndUrls = platformService.getPlatformsWithUrls();
        dto.setPlatformsAndUrls(platformsAndUrls);


        return dto;
    }




    /**
     * Return a formatted string representing the host and passage
     * @param hostStrain    the key to the map of host strains
     * @param hostStrainMap the map containing all the host strains associated to the model
     * @return a formatted string representing the host strains
     */
    private String getHostStrainString(String hostStrain, Map<String, String> hostStrainMap) {
        String passage = hostStrainMap.get(hostStrain).equals("Not Specified") ? "" : "(" + hostStrainMap.get(hostStrain) + ")";
        String formatted = String.format("%s%s", hostStrain, passage);
        return formatted;
    }


    public Map findPatientPlatforms(String dataSource, String modelId){

        Map<String, String> platformMap = new HashMap<>();

        List<MolecularCharacterization> molecularCharacterizations = molecularCharacterizationRepository.findPatientPlatformByModelId(dataSource,modelId);

        for (MolecularCharacterization mc : molecularCharacterizations) {

            if(mc.getPlatform() != null){
                platformMap.put(mc.getPlatform().getName(), mc.getPlatform().getName());
            }

        }

        return platformMap;
    }

    public Map<String, Set<String>> findModelPlatformAndPassages(String dataSource, String modelId,String passage){

        /**
         * Used to store a technology String with their respective List of PDX Passages
         */
        Map<String, Set<String>> platformMap = new HashMap<>();

        /**
         * Retrieve all the technologies for that mouse model
         */
        List<Platform> platforms = platformRepository.findModelPlatformByModelId(dataSource,modelId);

        /**
         * For each of the technologies retrieve the list of PDX passages using the specimen repository
         */
        for (Platform platform : platforms) {

            List<Specimen> specimens = specimenRepository.findSpecimenBySourcePdxIdAndPlatform2(dataSource,modelId,platform.getName());

            Set<String> passagesList = new HashSet<>();
            for (Specimen specimen : specimens)
            {
                passagesList.add(specimen.getPassage()+"");
            }

            platformMap.put(platform.getName(), passagesList);
        }

        return platformMap;
    }


    public List<String> getModelsOriginatedFromSamePatient(String dataSource, String modelId) {

        return patientRepository.getModelsOriginatedFromSamePatientByDataSourceAndModelId(dataSource, modelId);
    }


    public List<DrugSummaryDTO> getDrugSummary(String dataSource, String modelId) {

        TreatmentSummary ts = treatmentSummaryRepository.findByDataSourceAndModelId(dataSource, modelId);

        List<DrugSummaryDTO> results = new ArrayList<>();

        if (ts != null && ts.getTreatmentProtocols() != null) {

            for (TreatmentProtocol tp : ts.getTreatmentProtocols()) {

                DrugSummaryDTO dto = new DrugSummaryDTO();
                dto.setDrugName(tp.getDrug());
                dto.setDose(tp.getDose());

                if (tp.getResponse() != null && tp.getResponse().getDescription() != null) {
                    dto.setResponse(tp.getResponse().getDescription());
                } else {
                    dto.setResponse("");
                }

                results.add(dto);
            }
        }

        return results;
    }


    public VariationDataDTO patientVariationDataByPlatform(String dataSource, String modelId, String technology,
                                                           String searchParam, int draw, String sortColumn, String sortDir, int start, int size) {

        //int recordsTotal = patientRepository.countByBySourcePdxIdAndPlatform(dataSource,modelId,technology,"");

        int recordsTotal = modelCreationRepository.variationCountByDataSourceAndPdxIdAndPlatform(dataSource, modelId, technology, "");

        int recordsFiltered = recordsTotal;

        if (!searchParam.isEmpty()) {
            recordsFiltered = modelCreationRepository.variationCountByDataSourceAndPdxIdAndPlatform(dataSource, modelId, technology, searchParam);
        }

        /**
         * Retrieve the Records based on search parameter
         */
        ModelCreation model = modelCreationRepository.findVariationBySourcePdxIdAndPlatform(dataSource, modelId, technology, searchParam, start, size);
        VariationDataDTO variationDataDTO = new VariationDataDTO();
        List<String[]> variationData = new ArrayList<>();

        if (model != null && model.getSample() != null) {

            variationData.addAll(buildUpDTO(model.getSample(), "", draw, recordsTotal, recordsFiltered));
        }

        variationDataDTO.setDraw(draw);
        variationDataDTO.setRecordsTotal(recordsTotal);
        variationDataDTO.setRecordsFiltered(recordsFiltered);
        variationDataDTO.setData(variationData);

        return variationDataDTO;

    }


    public VariationDataDTO variationDataByPlatform(String dataSource, String modelId, String technology, String passage, int start, int size,
                                                    String searchParam, int draw, String sortColumn, String sortDir) {

        /**
         * Set the Pagination parameters: start comes in as 0,10,20 e.t.c while pageable works in page batches 0,1,2,...
         */
        Sort.Direction direction = getSortDirection(sortDir);
        Pageable pageable = new PageRequest(start, size, direction, sortColumn);


        /**
         * 1st count all the records and set Total Records & Initialize Filtered Record as Total record
         */
        //int recordsTotal = specimenRepository.countBySearchParameterAndPlatform(dataSource,modelId,technology,passage,"");
        int recordsTotal = specimenRepository.countByPlatform(dataSource, modelId, technology, passage);

        int recordsFiltered = recordsTotal;

        /**
         * If search Parameter is not empty: Count and Reset the value of filtered records based on search Parameter
         */
        if (!searchParam.isEmpty()) {
            recordsFiltered = specimenRepository.countBySearchParameterAndPlatform(dataSource, modelId, technology, passage, searchParam);
        }

        /**
         * Retrieve the Records based on search parameter
         */
        Page<Specimen> specimens = specimenRepository.findSpecimenBySourcePdxIdAndPlatform(dataSource, modelId, technology, passage, searchParam, pageable);
        VariationDataDTO variationDataDTO = new VariationDataDTO();
        List<String[]> variationData = new ArrayList();

        if (specimens != null) {
            for (Specimen specimen : specimens) {
                variationData.addAll(buildUpDTO(specimen.getSample(), specimen.getPassage(), draw, recordsTotal, recordsFiltered));
            }
        }


        variationDataDTO.setDraw(draw);
        variationDataDTO.setRecordsTotal(recordsTotal);
        variationDataDTO.setRecordsFiltered(recordsFiltered);
        variationDataDTO.setData(variationData);

        return variationDataDTO;

    }


    public List<String[]> buildUpDTO(Sample sample,String passage,int draw,int recordsTotal,int recordsFiltered){

        List<String[]> variationData = new LinkedList<>();

        /**
         * Generate an equivalent 2-D array type of the Retrieved result Set, the Front end table must be a 2D JSON Array
         */
        try {

            for (MolecularCharacterization dMolChar : sample.getMolecularCharacterizations()) {

                List<MarkerAssociation> markerAssociation = new ArrayList();// = dMolChar.getMarkerAssociations();
                markerAssociation.addAll(dMolChar.getMarkerAssociations());

                for (MarkerAssociation markerAssoc : markerAssociation) {

                    String[] markerAssocArray = new String[13];
                    markerAssocArray[0] = sample.getSourceSampleId();
                    markerAssocArray[1] = markerAssoc.getChromosome();
                    markerAssocArray[2] = markerAssoc.getSeqPosition();
                    markerAssocArray[3] = markerAssoc.getRefAllele();
                    markerAssocArray[4] = markerAssoc.getAltAllele();
                    markerAssocArray[5] = markerAssoc.getConsequence();
                    markerAssocArray[6] = markerAssoc.getMarker().getSymbol();
                    markerAssocArray[7] = markerAssoc.getAminoAcidChange();
                    markerAssocArray[8] = markerAssoc.getReadDepth();
                    markerAssocArray[9] = markerAssoc.getAlleleFrequency();
                    markerAssocArray[10] = markerAssoc.getRsVariants();
                    markerAssocArray[11] = dMolChar.getPlatform().getName();
                    markerAssocArray[12] = passage;
                    //markerAssocArray[13] = sample.getDiagnosis();
                    // markerAssocArray[14] = sample.getType().getName();

                    variationData.add(markerAssocArray);

                }
            }


        }catch (Exception e) { }


        return variationData;
    }



    public Sort.Direction getSortDirection(String sortDir){

        Sort.Direction direction = Sort.Direction.ASC;

        if (sortDir.equals("desc")){
            direction = Sort.Direction.DESC;
        }

        return direction;
    }



    public String notEmpty(String incoming){

        String result = (incoming == null) ? "Not Specified" : incoming;
        result = (result.length() == 0 ? "Not Specified" : result);

        return result;
    }


}
