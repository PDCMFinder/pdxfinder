package org.pdxfinder.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.pdxfinder.graph.dao.MarkerAssociation;
import org.pdxfinder.graph.dao.ModelCreation;
import org.pdxfinder.graph.dao.MolecularCharacterization;
import org.pdxfinder.graph.dao.Sample;
import org.pdxfinder.graph.repositories.ModelCreationRepository;
import org.pdxfinder.graph.repositories.OntologyTermRepository;
import org.pdxfinder.services.ds.FacetOption;
import org.pdxfinder.services.ds.ModelForQuery;
import org.pdxfinder.services.ds.SearchDS;
import org.pdxfinder.services.ds.SearchFacetName;
import org.pdxfinder.services.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {


    private final static Logger logger = LoggerFactory.getLogger(SearchService.class);

    private ModelCreationRepository modelCreationRepository;
    private OntologyTermRepository ontologyTermRepositoryRepository;
    private AutoCompleteService autoCompleteService;
    private Map<String, List<String>> facets = new HashMap<>();
    private MolCharService molCharService;
    private PlatformService platformService;
    private DrugService drugService;

    private SearchDS searchDS;

    List<String> patientAgeOptions = new ArrayList<>();
    List<String> datasourceOptions = new ArrayList<>();
    List<String> cancerBySystemOptions = new ArrayList<>();
    List<String> patientGenderOptions = new ArrayList<>();
    List<String> sampleTumorTypeOptions = new ArrayList<>();
    List<String> dataAvailableOptions = new ArrayList<>();




    public SearchService(ModelCreationRepository modelCreationRepository,
                         OntologyTermRepository ontologyTermRepository,
                         AutoCompleteService autoCompleteService,
                         MolCharService molCharService,
                         PlatformService platformService,
                         DrugService drugService,
                         SearchDS searchDS) {

        this.modelCreationRepository = modelCreationRepository;
        this.ontologyTermRepositoryRepository = ontologyTermRepository;
        this.autoCompleteService = autoCompleteService;
        this.molCharService = molCharService;
        this.platformService = platformService;
        this.drugService = drugService;

        this.searchDS = searchDS;

        facets.put("datasource_options", datasourceOptions);
        facets.put("patient_age_options", patientAgeOptions);
        facets.put("patient_gender_options", patientGenderOptions);
        facets.put("cancer_system_options", cancerBySystemOptions);
        facets.put("data_available_options", dataAvailableOptions);
        facets.put("sample_tumor_type_options", sampleTumorTypeOptions);

    }





    public WebSearchDTO webSearch(Optional<String> query,
                                  Optional<List<String>> datasource,
                                  Optional<List<String>> diagnosis,
                                  Optional<List<String>> patient_age,
                                  Optional<List<String>> patient_treatment,
                                  Optional<List<String>> patient_treatment_status,
                                  Optional<List<String>> patient_gender,
                                  Optional<List<String>> sample_origin_tissue,
                                  Optional<List<String>> cancer_system,
                                  Optional<List<String>> sample_tumor_type,
                                  Optional<List<String>> mutation,
                                  Optional<List<String>> drug,
                                  Optional<List<String>> project,
                                  Optional<List<String>> data_available,
                                  Optional<List<String>> breast_cancer_markers,
                                  Optional<List<String>> copy_number_alteration,
                                  Optional<List<String>> gene_expression,
                                  Optional<List<String>> cytogenetics,
                                  Integer page,
                                  Integer size){


        Map<SearchFacetName, List<String>> configuredFacets = getFacetMap(
                query,
                datasource,
                diagnosis,
                patient_age,
                patient_treatment,
                patient_treatment_status,
                patient_gender,
                sample_origin_tissue,
                cancer_system,
                sample_tumor_type,
                mutation,
                drug,
                project,
                data_available,
                breast_cancer_markers,
                copy_number_alteration,
                gene_expression,
                cytogenetics
        );

        WebSearchDTO wsDTO = new WebSearchDTO();

        //PERFORM SEARCH
        Set<ModelForQuery> results = searchDS.search(configuredFacets);

        //UPDATE SEARCH FILTERS (what is selected)
        wsDTO.setWebFacetsContainer(searchDS.getUpdatedSelectedFilters(configuredFacets));

        //UPDATE FACET STRING
        String facetString = getFacetString(configuredFacets);
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


        int begin = Math.max(1, current - 2);
        wsDTO.setBeginIndex(begin);

        int end = Math.min(begin + 4, numPages);
        wsDTO.setEndIndex(end);

        wsDTO.setPage(page);
        wsDTO.setSize(size);

        if(query.isPresent()){
            wsDTO.setQuery(query.get());
        }

        String textSearchDescription = getTextualDescription(facetString, results);

        if(textSearchDescription == null){
            textSearchDescription = "PDXFinder contains "+searchDS.getModels().size()+" models";
        }
        wsDTO.setTextSearchDescription(textSearchDescription);
        wsDTO.setTotalResults(searchDS.getModels().size());

        wsDTO.setMainSearchFieldOptions(autoCompleteService.getAutoSuggestions());

        List<ModelForQuery> resultSet = new ArrayList<>(results).subList((page - 1) * size, Math.min(((page - 1) * size) + size, results.size()));

        wsDTO.setResults(resultSet);

        //Update results table headers
        wsDTO.setAdditionalResultTableHeaders(getNewResultHeaders(configuredFacets));


        return wsDTO;
    }



    public ExportDTO export(Optional<String> query,
                            Optional<List<String>> datasource,
                            Optional<List<String>> diagnosis,
                            Optional<List<String>> patient_age,
                            Optional<List<String>> patient_treatment,
                            Optional<List<String>> patient_treatment_status,
                            Optional<List<String>> patient_gender,
                            Optional<List<String>> sample_origin_tissue,
                            Optional<List<String>> cancer_system,
                            Optional<List<String>> sample_tumor_type,
                            Optional<List<String>> mutation,
                            Optional<List<String>> drug,
                            Optional<List<String>> project,
                            Optional<List<String>> data_available,
                            Optional<List<String>> breast_cancer_markers,
                            Optional<List<String>> copy_number_alteration,
                            Optional<List<String>> gene_expression,
                            Optional<List<String>> cytogenetics){

        Map<SearchFacetName, List<String>> configuredFacets = getFacetMap(
                query,
                datasource,
                diagnosis,
                patient_age,
                patient_treatment,
                patient_treatment_status,
                patient_gender,
                sample_origin_tissue,
                cancer_system,
                sample_tumor_type,
                mutation,
                drug,
                project,
                data_available,
                breast_cancer_markers,
                copy_number_alteration,
                gene_expression,
                cytogenetics

        );

        ExportDTO eDTO = new ExportDTO();
        eDTO.setResults(searchDS.search(configuredFacets));
        eDTO.setFacetsString(configuredFacets.toString());

        return eDTO;
    }



    private Map<SearchFacetName, List<String>> getFacetMap(
            Optional<String> query,
            Optional<List<String>> datasource,
            Optional<List<String>> diagnosis,
            Optional<List<String>> patientAge,
            Optional<List<String>> patientTreatment,
            Optional<List<String>> patientTreatmentStatus,
            Optional<List<String>> patientGender,
            Optional<List<String>> sampleOriginTissue,
            Optional<List<String>> cancerSystem,
            Optional<List<String>> sampleTumorType,
            Optional<List<String>> mutation,
            Optional<List<String>> drug,
            Optional<List<String>> project,
            Optional<List<String>> data_available,
            Optional<List<String>> breast_cancer_markers,
            Optional<List<String>> copy_number_alteration,
            Optional<List<String>> gene_expression,
            Optional<List<String>> cytogenetics


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

        if (patientTreatment.isPresent() && !patientTreatment.get().isEmpty()) {
            configuredFacets.put(SearchFacetName.patient_treatment, new ArrayList<>());
            for (String s : patientTreatment.get()) {
                configuredFacets.get(SearchFacetName.patient_treatment).add(s);
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

        if (drug.isPresent() && !drug.get().isEmpty()) {
            configuredFacets.put(SearchFacetName.drug, new ArrayList<>());
            for (String s : drug.get()) {
                configuredFacets.get(SearchFacetName.drug).add(s);
            }
        }

        if (project.isPresent() && !project.get().isEmpty()) {
            configuredFacets.put(SearchFacetName.project, new ArrayList<>());
            for (String s : project.get()) {
                configuredFacets.get(SearchFacetName.project).add(s);
            }
        }

        if (data_available.isPresent() && !data_available.get().isEmpty()) {
            configuredFacets.put(SearchFacetName.data_available, new ArrayList<>());
            for (String s : data_available.get()) {
                configuredFacets.get(SearchFacetName.data_available).add(s);
            }
        }

        if (breast_cancer_markers.isPresent() && !breast_cancer_markers.get().isEmpty()) {
            configuredFacets.put(SearchFacetName.breast_cancer_markers, new ArrayList<>());
            for (String s : breast_cancer_markers.get()) {
                configuredFacets.get(SearchFacetName.breast_cancer_markers).add(s);
            }
        }

        if (copy_number_alteration.isPresent() && !copy_number_alteration.get().isEmpty()) {
            configuredFacets.put(SearchFacetName.copy_number_alteration, new ArrayList<>());
            for (String s : copy_number_alteration.get()) {
                configuredFacets.get(SearchFacetName.copy_number_alteration).add(s);
            }
        }

        if (gene_expression.isPresent() && !gene_expression.get().isEmpty()) {
            configuredFacets.put(SearchFacetName.gene_expression, new ArrayList<>());
            for (String s : gene_expression.get()) {
                configuredFacets.get(SearchFacetName.gene_expression).add(s);
            }
        }

        if (cytogenetics.isPresent() && !cytogenetics.get().isEmpty()) {
            configuredFacets.put(SearchFacetName.cytogenetics, new ArrayList<>());
            for (String s : cytogenetics.get()) {
                configuredFacets.get(SearchFacetName.cytogenetics).add(s);
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
    private String getFacetString(Map<SearchFacetName, List<String>> allSelectedFacetOptions) {

        List<String> pieces = new ArrayList<>();

        for (Map.Entry<SearchFacetName,List<String>> facet : allSelectedFacetOptions.entrySet()) {

            for(String filterVal : facet.getValue()){

                pieces.add(facet.getKey().getName() + "=" +filterVal);
            }

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



    public List<String> getNewResultHeaders(Map<SearchFacetName, List<String>> filters){

        List<String> headers= new ArrayList<>();

        for (SearchFacetName facet : filters.keySet()) {


            switch(facet){

                case mutation:
                    headers.add("MUTATIONS");
                    break;

                case drug:
                    headers.add("DRUG AND RESPONSE");
                    break;

                case breast_cancer_markers:
                    headers.add("BREAST CANCER MARKERS");
                    break;

                case patient_treatment:
                    headers.add("PATIENT TREATMENT");
                    break;

                case patient_treatment_status:
                    headers.add("PATIENT TREATMENT STATUS");
                    break;

                case patient_gender:
                    headers.add("SEX");
                    break;

                case patient_age:
                    headers.add("AGE");
                    break;
                case copy_number_alteration:
                    headers.add("COPY NUMBER ALTERATION");
                    break;
                case gene_expression:
                    headers.add("GENE EXPRESSION");
                    break;
                case cytogenetics:
                    headers.add("CYTOGENETICS");
                    break;


            }

        }

        if(headers.isEmpty()){
            headers.add("DATA AVAILABLE");
        }

        return headers;
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

            if (model.getSample() != null && model.getSample().getSampleToOntologyRelationship() != null) {
                sdto.setMappedOntology(model.getSample().getSampleToOntologyRelationship().getOntologyTerm().getLabel());
            }

            sdto.setSearchParameter(query);


            if (model.getSample() != null && model.getSample().getMolecularCharacterizations() != null) {
                Set<String> markerSet = new HashSet<>();

                for (MolecularCharacterization mc : model.getSample().getMolecularCharacterizations()) {
                    markerSet.addAll(mc.getMarkers());
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


    private Map<String, List<DrugSummaryDTO>> getDrugSummaryMap(List<ModelForQuery> resultSet){


        return new HashMap<>();
    }



    public List<ModelCreation> getModelsByMolcharType(String type) {

        List<ModelCreation> models = modelCreationRepository.findByMolcharType(type);

        return models;
    }


}



