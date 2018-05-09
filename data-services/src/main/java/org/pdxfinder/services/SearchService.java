package org.pdxfinder.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.pdxfinder.dao.MarkerAssociation;
import org.pdxfinder.dao.ModelCreation;
import org.pdxfinder.dao.MolecularCharacterization;
import org.pdxfinder.repositories.*;
import org.pdxfinder.services.ds.FacetOption;
import org.pdxfinder.services.ds.ModelForQuery;
import org.pdxfinder.services.ds.SearchDS;
import org.pdxfinder.services.ds.SearchFacetName;
import org.pdxfinder.services.dto.ExportDTO;
import org.pdxfinder.services.dto.SearchDTO;
import org.pdxfinder.services.dto.WebSearchDTO;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {


    private ModelCreationRepository modelCreationRepository;
    private OntologyTermRepository ontologyTermRepositoryRepository;
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



    public SearchService(ModelCreationRepository modelCreationRepository,
                         OntologyTermRepository ontologyTermRepository,
                         AutoCompleteService autoCompleteService,
                         MolCharService molCharService,
                         PlatformService platformService,
                         SearchDS searchDS) {

        this.modelCreationRepository = modelCreationRepository;
        this.ontologyTermRepositoryRepository = ontologyTermRepository;
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




}



