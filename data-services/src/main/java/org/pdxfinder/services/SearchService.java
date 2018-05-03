package org.pdxfinder.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.pdxfinder.dao.*;
import org.pdxfinder.repositories.*;
import org.pdxfinder.services.ds.FacetOption;
import org.pdxfinder.services.ds.ModelForQuery;
import org.pdxfinder.services.ds.SearchDS;
import org.pdxfinder.services.ds.SearchFacetName;
import org.pdxfinder.services.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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
    private TreatmentSummaryRepository treatmentSummaryRepository;

    private GraphService graphService;
    private AutoCompleteService autoCompleteService;
    private Map<String, List<String>> facets = new HashMap<>();
    private MolCharService molCharService;
    private PlatformService platformService;

    private SearchDS searchDS;

    List<String> patientAgeOptions = SearchDS.PATIENT_AGE_OPTIONS;
    List<String> datasourceOptions = SearchDS.DATASOURCE_OPTIONS;
    List<String> cancerBySystemOptions = SearchDS.CANCERS_BY_SYSTEM_OPTIONS;
    List<String> patientGenderOptions = SearchDS.PATIENT_GENDERS;
    List<String> sampleTumorTypeOptions = SearchDS.SAMPLE_TUMOR_TYPE_OPTIONS;
    List<String> diagnosisOptions = SearchDS.DIAGNOSIS_OPTIONS;


    private final String JAX_URL = "http://tumor.informatics.jax.org/mtbwi/pdxDetails.do?modelID=";
    private final String JAX_URL_TEXT = "View data at JAX";
    private final String IRCC_URL = "mailto:andrea.bertotti@unito.it?subject=";
    private final String IRCC_URL_TEXT = "Contact IRCC here";
    private final String PDMR_URL = "https://pdmdb.cancer.gov/pls/apex/f?p=101:41";
    private final String PDMR_URL_TEXT = "View data at PDMR";
    
    // for PDXNet
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



    public SearchService(SampleRepository sampleRepository,
                         PatientRepository patientRepository,
                         PatientSnapshotRepository patientSnapshotRepository,
                         ModelCreationRepository modelCreationRepository,
                         OntologyTermRepository ontologyTermRepository,
                         SpecimenRepository specimenRepository,
                         MolecularCharacterizationRepository molecularCharacterizationRepository,
                         PlatformRepository platformRepository,
                         TreatmentSummaryRepository treatmentSummaryRepository,
                         GraphService graphService,
                         AutoCompleteService autoCompleteService,
                         MolCharService molCharService,
                         PlatformService platformService,
                         SearchDS searchDS) {

        this.sampleRepository = sampleRepository;
        this.patientRepository = patientRepository;
        this.patientSnapshotRepository = patientSnapshotRepository;
        this.modelCreationRepository = modelCreationRepository;
        this.ontologyTermRepositoryRepository = ontologyTermRepository;
        this.molecularCharacterizationRepository = molecularCharacterizationRepository;
        this.specimenRepository = specimenRepository;
        this.platformRepository = platformRepository;
        this.treatmentSummaryRepository = treatmentSummaryRepository;
        this.graphService = graphService;
        this.autoCompleteService = autoCompleteService;
        this.molCharService = molCharService;
        this.platformService = platformService;

        this.searchDS = searchDS;

        facets.put("datasource_options", datasourceOptions);
        facets.put("patient_age_options", patientAgeOptions);
        facets.put("patient_gender_options", patientGenderOptions);
        facets.put("cancer_system_options", cancerBySystemOptions);
        facets.put("sample_tumor_type_options", sampleTumorTypeOptions);
    }



    public ExportDTO export(Optional<String> query,
                                     Optional<List<String>> datasource,
                                     Optional<List<String>> diagnosis,
                                     Optional<List<String>> patient_age,
                                     Optional<List<String>> patient_treatment_status,
                                     Optional<List<String>> patient_gender,
                                     Optional<List<String>> sample_origin_tissue,
                                     Optional<List<String>> cancer_system,
                                     Optional<List<String>> sample_tumor_type,
                                     Optional<List<String>> mutation){

        Map<SearchFacetName, List<String>> configuredFacets = getFacetMap(
                query,
                datasource,
                diagnosis,
                patient_age,
                patient_treatment_status,
                patient_gender,
                sample_origin_tissue,
                cancer_system,
                sample_tumor_type,
                mutation
        );

        ExportDTO eDTO = new ExportDTO();
        eDTO.setResults(searchDS.search(configuredFacets));
        eDTO.setFacetsString(configuredFacets.toString());

        return eDTO;
    }


    public WebSearchDTO webSearch(Optional<String> query,
                                  Optional<List<String>> datasource,
                                  Optional<List<String>> diagnosis,
                                  Optional<List<String>> patient_age,
                                  Optional<List<String>> patient_treatment_status,
                                  Optional<List<String>> patient_gender,
                                  Optional<List<String>> sample_origin_tissue,
                                  Optional<List<String>> cancer_system,
                                  Optional<List<String>> sample_tumor_type,
                                  Optional<List<String>> mutation,
                                  Integer page,
                                  Integer size){


        Map<SearchFacetName, List<String>> configuredFacets = getFacetMap(
                query,
                datasource,
                diagnosis,
                patient_age,
                patient_treatment_status,
                patient_gender,
                sample_origin_tissue,
                cancer_system,
                sample_tumor_type,
                mutation
        );

        WebSearchDTO wsDTO = new WebSearchDTO();


        Set<ModelForQuery> results = searchDS.search(configuredFacets);

        List<FacetOption> patientAgeSelected = searchDS.getFacetOptions(SearchFacetName.patient_age, patientAgeOptions, results, patient_age.orElse(null));
        List<FacetOption> patientGenderSelected = searchDS.getFacetOptions(SearchFacetName.patient_gender, patientGenderOptions, results, patient_gender.orElse(null));
        List<FacetOption> datasourceSelected = searchDS.getFacetOptions(SearchFacetName.datasource, datasourceOptions, results, datasource.orElse(null));
        List<FacetOption> cancerSystemSelected = searchDS.getFacetOptions(SearchFacetName.cancer_system, cancerBySystemOptions, results, cancer_system.orElse(null));
        List<FacetOption> sampleTumorTypeSelected = searchDS.getFacetOptions(SearchFacetName.sample_tumor_type, sampleTumorTypeOptions, results, sample_tumor_type.orElse(null));
        List<FacetOption> mutationSelected = searchDS.getFacetOptions(SearchFacetName.mutation, null, results, mutation.orElse(null));



        wsDTO.setPatientAgeSelected(patientAgeSelected);
        wsDTO.setPatientGenderSelected(patientGenderSelected);
        wsDTO.setDatasourceSelected(datasourceSelected);
        wsDTO.setCancerSystemSelected(cancerSystemSelected);
        wsDTO.setSampleTumorTypeSelected(sampleTumorTypeSelected);
        wsDTO.setMutationSelected(mutationSelected);



        // Only add diagnosisSelected if diagnosis has actually been specified
        List<FacetOption> diagnosisSelected = null;
        if (diagnosis.isPresent()) {
            diagnosisSelected = searchDS.getFacetOptions(SearchFacetName.diagnosis, null, results, diagnosis.orElse(null));
        }

        // Ensure to add the facet options to this list so the URL encoding retains the configured options
        String facetString = getFacetString(
                new HashSet<>(
                        Arrays.asList(
                                patientAgeSelected,
                                patientGenderSelected,
                                datasourceSelected,
                                cancerSystemSelected,
                                sampleTumorTypeSelected,
                                mutationSelected

                        )
                )
        );

        // If there is a query, append the query parameter to any configured facet string
        if (query.isPresent() && !query.get().isEmpty()) {
            facetString = StringUtils.join(Arrays.asList("query=" + query.get(), facetString), "&");
        }

        // If there is a diagnosis, append the diagnosis parameters to any configured facet string
        if (diagnosis.isPresent() && !diagnosis.get().isEmpty()) {
            for (String diag : diagnosis.get()) {
                facetString = StringUtils.join(Arrays.asList("diagnosis=" + diag, facetString), "&");
            }
        }


        if (mutation.isPresent() && !mutation.get().isEmpty()) {
            List<String> mutList = new ArrayList<>();
            for (String mut : mutation.get()) {
                mutList.add("mutation=" + mut);
            }

            if (facetString.length() != 0 && !facetString.endsWith("&")) {
                facetString += "&";
            }
            for (String mut : mutList) {
                facetString += mut + "&";
            }

        }

        wsDTO.setFacetString(facetString);



        // Num pages is converted to an int using this formula int n = a / b + (a % b == 0) ? 0 : 1;
        int numPages = results.size() / size + (results.size() % size == 0 ? 0 : 1);

        // If there are no results, default to 1 page (instead of 0 pages)
        if (numPages < 1) {
            numPages = 1;
        }

        wsDTO.setNumPages(numPages);

        int current = page;
        wsDTO.setCurrentIndex(current);


        int begin = Math.max(1, current - 4);
        wsDTO.setBeginIndex(begin);

        int end = Math.min(begin + 7, numPages);
        wsDTO.setEndIndex(end);

        String mutatedMarkers = molCharService.getMutatedMarkersAndVariants();
        wsDTO.setMutatedMarkersAndVariants(mutatedMarkers);

        String textSearchDescription = getTextualDescription(facetString, results);
        wsDTO.setTextSearchDescription(textSearchDescription);

        boolean mutSelected = false;

        if(mutation.isPresent() && !mutation.get().isEmpty()){
            mutSelected = true;
        }

        wsDTO.setAutoCompleteOptions(autoCompleteService.getAutoSuggestions());
        List<ModelForQuery> resultSet = new ArrayList<>(results).subList((page - 1) * size, Math.min(((page - 1) * size) + size, results.size()));

        wsDTO.setSearchResults(resultSet);
        wsDTO.setPlatformsAndUrls(platformService.getPlatformsWithUrls());

        if (mutSelected == true){
            wsDTO.setPlatformMap(getPlatformOrMutationFromMutatedVariants(resultSet,"platformMap"));
            wsDTO.setMutationMap(getPlatformOrMutationFromMutatedVariants(resultSet,"mutationMap"));
        }

        wsDTO.setIsMutationSelected(mutSelected);
        wsDTO.setQuery(query.orElse(""));
        wsDTO.setTotalResults(results.size());
        wsDTO.setPage(page);
        wsDTO.setSize(size);
        wsDTO.setDiagnosisSelected(diagnosisSelected);
        wsDTO.setFacetOptions(facets);


        ObjectMapper mapper = new ObjectMapper();
        Map<String, List<String>> mapObject = new HashMap<>();
        try{
            mapObject = mapper.readValue(mutatedMarkers, Map.class);
        }catch (Exception e){}


        String done = "";
        Map<String, List<String>> userChoice = new HashMap<>();
        Map<String, Set<String>> allVariants = new LinkedHashMap<>();

        try {
            for (String markerReq : mutation.get()) {
                String marka = markerReq.split("___")[0];
                List<String> variantList = new ArrayList<>();
                String variant = "";

                if (!done.contains(marka)) { // New Marker
                    for (String markerReq2 : mutation.get()) {
                        if (marka.equals(markerReq2.split("___")[0])){
                            variant = markerReq2.split("___")[2];
                            if (variant.equals("ALL")){
                                variantList = mapObject.get(marka);
                            }
                            else {
                                variantList.add(variant);
                            }
                        }
                    }
                    userChoice.put(marka,variantList);

                    Set<String> sortedVariantList = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
                    sortedVariantList.addAll(mapObject.get(marka));

                    allVariants.put(marka,sortedVariantList);
                }

                done += marka;
            }
        }catch (Exception e){}

        wsDTO.setMarkerMap(userChoice);
        wsDTO.setMarkerMapWithAllVariants(allVariants);


        return wsDTO;
    }


    private Map<SearchFacetName, List<String>> getFacetMap(
            Optional<String> query,
            Optional<List<String>> datasource,
            Optional<List<String>> diagnosis,
            Optional<List<String>> patientAge,
            Optional<List<String>> patientTreatmentStatus,
            Optional<List<String>> patientGender,
            Optional<List<String>> sampleOriginTissue,
            Optional<List<String>> cancerSystem,
            Optional<List<String>> sampleTumorType,
            Optional<List<String>> mutation


    ) {

        Map<SearchFacetName, List<String>> configuredFacets = new HashMap<>();

        if (query.isPresent() && !query.get().isEmpty()) {
            configuredFacets.put(SearchFacetName.query, new ArrayList<>());
            configuredFacets.get(SearchFacetName.query).add(query.get());
        }

        if (datasource.isPresent() && !datasource.get().isEmpty()) {
            configuredFacets.put(SearchFacetName.datasource, new ArrayList<>());
            for (String s : datasource.get()) {
                configuredFacets.get(SearchFacetName.datasource).add(s);
            }
        }

        if (diagnosis.isPresent() && !diagnosis.get().isEmpty()) {
            configuredFacets.put(SearchFacetName.diagnosis, new ArrayList<>());
            for (String s : diagnosis.get()) {
                configuredFacets.get(SearchFacetName.diagnosis).add(s);
            }
        }

        if (patientAge.isPresent() && !patientAge.get().isEmpty()) {
            configuredFacets.put(SearchFacetName.patient_age, new ArrayList<>());
            for (String s : patientAge.get()) {
                configuredFacets.get(SearchFacetName.patient_age).add(s);
            }
        }

        if (patientTreatmentStatus.isPresent() && !patientTreatmentStatus.get().isEmpty()) {
            configuredFacets.put(SearchFacetName.patient_treatment_status, new ArrayList<>());
            for (String s : patientTreatmentStatus.get()) {
                configuredFacets.get(SearchFacetName.patient_treatment_status).add(s);
            }
        }

        if (patientGender.isPresent() && !patientGender.get().isEmpty()) {
            configuredFacets.put(SearchFacetName.patient_gender, new ArrayList<>());
            for (String s : patientGender.get()) {
                configuredFacets.get(SearchFacetName.patient_gender).add(s);
            }
        }

        if (sampleOriginTissue.isPresent() && !sampleOriginTissue.get().isEmpty()) {
            configuredFacets.put(SearchFacetName.sample_origin_tissue, new ArrayList<>());
            for (String s : sampleOriginTissue.get()) {
                configuredFacets.get(SearchFacetName.sample_origin_tissue).add(s);
            }
        }

        if (cancerSystem.isPresent() && !cancerSystem.get().isEmpty()) {
            configuredFacets.put(SearchFacetName.cancer_system, new ArrayList<>());
            for (String s : cancerSystem.get()) {
                configuredFacets.get(SearchFacetName.cancer_system).add(s);
            }
        }

        if (sampleTumorType.isPresent() && !sampleTumorType.get().isEmpty()) {
            configuredFacets.put(SearchFacetName.sample_tumor_type, new ArrayList<>());
            for (String s : sampleTumorType.get()) {
                configuredFacets.get(SearchFacetName.sample_tumor_type).add(s);
            }
        }

        if (mutation.isPresent() && !mutation.get().isEmpty()) {
            configuredFacets.put(SearchFacetName.mutation, new ArrayList<>());
            for (String s : mutation.get()) {
                configuredFacets.get(SearchFacetName.mutation).add(s);
            }
        }


        return configuredFacets;
    }

    /**
     * Get a string representation of all the configured facets
     *
     * @param allSelectedFacetOptions
     * @return
     */
    private String getFacetString(Set<List<FacetOption>> allSelectedFacetOptions) {
        List<String> pieces = new ArrayList<>();
        for (List<FacetOption> facetOptions : allSelectedFacetOptions) {
            pieces.add(facetOptions.stream()
                    .filter(x -> x.getSelected() != null)
                    .filter(FacetOption::getSelected)
                    .map(x -> x.getFacetType() + "=" + x.getName())
                    .collect(Collectors.joining("&")));
        }
        return pieces.stream().filter(x -> !x.isEmpty()).collect(Collectors.joining("&"));
    }

    public String getTextualDescription(String facetString, Set<ModelForQuery> results) {

        if (StringUtils.isEmpty(facetString)) {
            return null;
        }

        String textDescription = "Your filter for ";
        Map<String, Set<String>> filters = new LinkedHashMap<>();

        for (String urlParams : facetString.split("&")) {
            List<String> pieces = Arrays.asList(urlParams.split("="));
            String key = pieces.get(0);
            String value = pieces.get(1);
            String replacementValue;

            if (!filters.containsKey(key)) {
                filters.put(key, new TreeSet<>());
            }

            if(key.equals("mutation")){

                replacementValue = value.replace("___MUT___", " variant ");
                value = replacementValue;
            }

            filters.get(key).add(value);
        }

        Set<String> dataSourceCounter = new HashSet<>();
        for(ModelForQuery mfq :results){
            dataSourceCounter.add(mfq.getDatasource());
        }

        textDescription += StringUtils
                .join(filters.keySet()
                        .stream()
                        .map(x -> "<b>" + x + ":</b> (" + StringUtils.join(filters.get(x), ", ").replaceAll("\\[", "").replaceAll("\\]", "") + ")")
                        .collect(Collectors.toList()), ", ");

        textDescription += " returned " + results.size() + " result";
        textDescription += results.size() == 1 ? "" : "s";
        textDescription += " in "+dataSourceCounter.size()+" source";
        textDescription += dataSourceCounter.size() == 1 ? "" : "s";

        if(dataSourceCounter.size() != 0) textDescription += " "+dataSourceCounter.toString();

        return textDescription;

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

            if (model.getSample() != null && model.getSample().getSampleToOntologyRelationShip() != null) {
                sdto.setMappedOntology(model.getSample().getSampleToOntologyRelationShip().getOntologyTerm().getLabel());
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

    private Map<String, List<String>> getPlatformOrMutationFromMutatedVariants(List<ModelForQuery> resultSet, String whichMap){

        Map<String, List<String>> platformMap = new HashMap<>();
        Map<String, List<String>> mutationMap = new HashMap<>();

        for (ModelForQuery mfq : resultSet){

            List<String> dPlatforms = new ArrayList<>();
            List<String> dMutations = new ArrayList<>();

            for (String mutatedVariants : mfq.getMutatedVariants()){
                String[] mv = mutatedVariants.split("\\s+");  // e.g  [Truseq_JAX BRAF V600E, CTP BRAF V600E]
                dPlatforms.add(mv[0]);
                dMutations.add(mv[1]+" "+mv[2]);
            }

            platformMap.put(mfq.getExternalId(), dPlatforms);
            mutationMap.put(mfq.getExternalId(), dMutations);
        }

        if (whichMap.equals("platformMap")){
            return platformMap;
        }else{
            return mutationMap;
        }

    }


    public DetailsDTO searchForModel(String dataSource, String modelId, int page, int size,String technology,String passage,String searchFilter) {


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
                        dto.setEngraftmentSite(s.getEngraftmentSite().getName());
                    }
                    else{

                        dto.setEngraftmentSite("Not Specified");
                    }

                    if(s.getEngraftmentType() != null){
                        dto.setSampleType(s.getEngraftmentType().getName());
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

        dto.setStrain(composedStrain);


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


        return dto;
    }


    /**
     * Return a formatted string representing the host and passage
     *
     * @param hostStrain    the key to the map of host strains
     * @param hostStrainMap the map containing all the host strains associated to the model
     * @return a formatted string representing the host strains
     */
    private String getHostStrainString(String hostStrain, Map<String, String> hostStrainMap) {
        String passage = hostStrainMap.get(hostStrain).equals("Not Specified") ? "" : "(" + hostStrainMap.get(hostStrain) + ")";
        String formatted = String.format("%s%s", hostStrain, passage);
        return formatted;
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


    public VariationDataDTO patientVariationDataByPlatform(String dataSource, String modelId, String technology,
                                                           String searchParam, int draw, String sortColumn, String sortDir, int start, int size) {

        //int recordsTotal = patientRepository.countByBySourcePdxIdAndPlatform(dataSource,modelId,technology,"");

        int recordsTotal = modelCreationRepository.variationCountByDataSourceAndPdxIdAndPlatform(dataSource,modelId,technology,"");

        int recordsFiltered = recordsTotal;

        if (!searchParam.isEmpty()) {
            recordsFiltered = modelCreationRepository.variationCountByDataSourceAndPdxIdAndPlatform(dataSource,modelId,technology,searchParam);
        }

        /**
         * Retrieve the Records based on search parameter
         */
        ModelCreation model = modelCreationRepository.findVariationBySourcePdxIdAndPlatform(dataSource,modelId,technology,searchParam,start,size);
        VariationDataDTO variationDataDTO = new VariationDataDTO();
        List<String[]> variationData = new ArrayList<>();

        if (model != null && model.getSample() != null ) {

            variationData.addAll(buildUpDTO(model.getSample(),"",draw,recordsTotal,recordsFiltered));
        }

        variationDataDTO.setDraw(draw);
        variationDataDTO.setRecordsTotal(recordsTotal);
        variationDataDTO.setRecordsFiltered(recordsFiltered);
        variationDataDTO.setData(variationData);

        return variationDataDTO;

    }


    public VariationDataDTO variationDataByPlatform(String dataSource, String modelId, String technology,String passage, int start, int size,
                                                    String searchParam, int draw, String sortColumn, String sortDir) {

        /**
         * Set the Pagination parameters: start comes in as 0,10,20 e.t.c while pageable works in page batches 0,1,2,...
         */
        Sort.Direction direction = getSortDirection(sortDir);
        Pageable pageable = new PageRequest(start,size, direction,sortColumn);


        /**
         * 1st count all the records and set Total Records & Initialize Filtered Record as Total record
         */
        //int recordsTotal = specimenRepository.countBySearchParameterAndPlatform(dataSource,modelId,technology,passage,"");
        int recordsTotal = specimenRepository.countByPlatform(dataSource,modelId,technology,passage);

        int recordsFiltered = recordsTotal;

        /**
         * If search Parameter is not empty: Count and Reset the value of filtered records based on search Parameter
         */
        if (!searchParam.isEmpty()) {
            recordsFiltered = specimenRepository.countBySearchParameterAndPlatform(dataSource,modelId,technology,passage,searchParam);
        }

        /**
         * Retrieve the Records based on search parameter
         */
        Page<Specimen> specimens = specimenRepository.findSpecimenBySourcePdxIdAndPlatform(dataSource,modelId,technology,passage,searchParam,pageable);
        VariationDataDTO variationDataDTO = new VariationDataDTO();
        List<String[]> variationData = new ArrayList();

        if (specimens != null) {
            for (Specimen specimen : specimens) {
                variationData.addAll( buildUpDTO(specimen.getSample(),specimen.getPassage(),draw,recordsTotal,recordsFiltered) );
            }
        }



        variationDataDTO.setDraw(draw);
        variationDataDTO.setRecordsTotal(recordsTotal);
        variationDataDTO.setRecordsFiltered(recordsFiltered);
        variationDataDTO.setData(variationData);

        return variationDataDTO;

    }


    public List<String> getModelsOriginatedFromSamePatient(String dataSource, String modelId){

        return patientRepository.getModelsOriginatedFromSamePatientByDataSourceAndModelId(dataSource, modelId);
    }


    public List<DrugSummaryDTO> getDrugSummary(String dataSource, String modelId){

        TreatmentSummary ts = treatmentSummaryRepository.findByDataSourceAndModelId(dataSource, modelId);

        List<DrugSummaryDTO> results = new ArrayList<>();

        if(ts != null && ts.getTreatmentProtocols() != null){

            for(TreatmentProtocol tp : ts.getTreatmentProtocols()){

                DrugSummaryDTO dto = new DrugSummaryDTO();
                dto.setDrugName(tp.getDrug());
                dto.setDose(tp.getDose());

                if(tp.getResponse() != null && tp.getResponse().getDescription() != null){
                    dto.setResponse(tp.getResponse().getDescription());
                }
                else{
                    dto.setResponse("");
                }

                results.add(dto);
            }
        }

        return results;
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




}



