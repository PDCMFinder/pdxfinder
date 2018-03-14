package org.pdxfinder.services.ds;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONException;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.dao.*;
import org.pdxfinder.repositories.DataProjectionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Long.parseLong;


/*
 * Created by csaba on 19/01/2018.
 */

@Component
public class SearchDS {

    private final static Logger log = LoggerFactory.getLogger(SearchDS.class);

    private Set<ModelForQuery> models;
    private Map<String, String> cancerSystemMap = new HashMap<>();

    //marker=> {variant=>set of model ids}
    private Map<String, Map<String, Set<Long>>> mutations = new HashMap<String, Map<String, Set<Long>>>();


    private DataProjectionRepository dataProjectionRepository;

    public static List<String> PATIENT_AGE_OPTIONS = Arrays.asList(
            "0-9",
            "10-19",
            "20-29",
            "30-39",
            "40-49",
            "50-59",
            "60-69",
            "70-79",
            "80-89",
            "90",
            "Not Specified"
    );
    public static List<String> DATASOURCE_OPTIONS = Arrays.asList(
            "JAX",
            "IRCC",
            "PDMR",
            "PDXNet-HCI-BCM",
            "PDXNet-MDAnderson",
            "PDXNet-WUSTL",
            "PDXNet-Wistar-MDAnderson-Penn"
    );
    public static List<String> PATIENT_GENDERS = Arrays.asList(
            "Male",
            "Female",
            "Not Specified"
    );


    public static List<String> CANCERS_BY_SYSTEM_OPTIONS = Arrays.asList(
            "Breast Cancer",
            "Cardiovascular Cancer",
            "Connective and Soft Tissue Cancer",
            "Digestive System Cancer",
            "Endocrine Cancer",
            "Eye Cancer",
            "Head and Neck Cancer",
            "Hematopoietic and Lymphoid System Cancer",
            "Nervous System Cancer",
            "Peritoneal and Retroperitoneal Cancer",
            "Reproductive System Cancer",
            "Respiratory Tract Cancer",
            "Thoracic Cancer",
            "Skin Cancer",
            "Urinary System Cancer",
            "Unclassified"
    );

    public static List<String> SAMPLE_TUMOR_TYPE_OPTIONS = Arrays.asList(
            "Primary",
            "Metastatic",
            "Recurrent",
            "Refractory",
            "Not Specified"
    );
    public static List<String> DIAGNOSIS_OPTIONS = new ArrayList<>();

    /**
     * Populate the complete set of models for searching when this object is instantiated
     */
    public SearchDS(DataProjectionRepository dataProjectionRepository) {
        Assert.notNull(dataProjectionRepository, "Data projection repository cannot be null");

        this.dataProjectionRepository = dataProjectionRepository;
        this.models = new HashSet<>();
    }

    @PostConstruct
    void initialize() {


        //this method loads the ModelForQuery Data Projection object and
        initializeModels();

        //loads the mutation map from Data Projection
        initializeMutations();


        List<String> padding = new ArrayList<>();
        padding.add("NO DATA");


        // Populate the list of possible diagnoses
        DIAGNOSIS_OPTIONS = models.stream().map(ModelForQuery::getDiagnosis).distinct().collect(Collectors.toList());


        //
        // Filter out all options that have no models for that option
        //
        PATIENT_AGE_OPTIONS = PATIENT_AGE_OPTIONS
                .stream()
                .filter(x -> models
                        .stream()
                        .map(ModelForQuery::getPatientAge)
                        .collect(Collectors.toSet())
                        .contains(x))
                .collect(Collectors.toList());

        DATASOURCE_OPTIONS = DATASOURCE_OPTIONS
                .stream()
                .filter(x -> models
                        .stream()
                        .map(ModelForQuery::getDatasource)
                        .collect(Collectors.toSet())
                        .contains(x))
                .collect(Collectors.toList());

        CANCERS_BY_SYSTEM_OPTIONS = CANCERS_BY_SYSTEM_OPTIONS
                .stream()
                .filter(x -> models
                        .stream()
                        .map(ModelForQuery::getCancerSystem)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toSet())
                        .contains(x))
                .collect(Collectors.toList());

        PATIENT_GENDERS = PATIENT_GENDERS
                .stream()
                .filter(x -> models
                        .stream()
                        .map(ModelForQuery::getPatientGender)
                        .collect(Collectors.toSet())
                        .contains(x))
                .collect(Collectors.toList());

        SAMPLE_TUMOR_TYPE_OPTIONS = SAMPLE_TUMOR_TYPE_OPTIONS
                .stream()
                .filter(x -> models
                        .stream()
                        .map(ModelForQuery::getSampleTumorType)
                        .collect(Collectors.toSet())
                        .contains(x))
                .collect(Collectors.toList());

        DIAGNOSIS_OPTIONS = DIAGNOSIS_OPTIONS
                .stream()
                .filter(x -> models
                        .stream()
                        .map(ModelForQuery::getDiagnosis)
                        .collect(Collectors.toSet())
                        .contains(x))
                .collect(Collectors.toList());

    }

    /**
     * Recursively get all ancestors starting from the supplied ontology term
     *
     * @param t the starting term in the ontology
     * @return a set of ontology terms corresponding to the ancestors of the term supplied
     */
    public Set<OntologyTerm> getAllAncestors(OntologyTerm t) {

        Set<OntologyTerm> retSet = new HashSet<>();

        // Store this ontology term in the set
        retSet.add(t);

        // If this term has parent terms
        if (t.getSubclassOf() != null && t.getSubclassOf().size() > 0) {

            // For each parent term
            for (OntologyTerm st : t.getSubclassOf()) {

                // Recurse and add all ancestor terms to the set
                retSet.addAll(getAllAncestors(st));
            }
        }

        // Return the full set
        return retSet;
    }

    public Set<ModelForQuery> getModels() {
        return models;
    }

    public void setModels(Set<ModelForQuery> models) {
        this.models = models;
    }


    /**
     * This method loads the ModelForQuery Data Projection object and initializes the models
     */
    void initializeModels() {


        String modelJson = dataProjectionRepository.findByLabel("ModelForQuery").getValue();

        try {
            JSONArray jarray = new JSONArray(modelJson);

            for (int i = 0; i < jarray.length(); i++) {

                JSONObject j = jarray.getJSONObject(i);

                ModelForQuery mfq = new ModelForQuery();

                mfq.setModelId(parseLong(j.getString("modelId")));
                mfq.setDatasource(j.getString("datasource"));
                mfq.setExternalId(j.getString("externalId"));
                mfq.setPatientAge(j.getString("patientAge"));
                mfq.setPatientGender(j.getString("patientGender"));
                mfq.setSampleOriginTissue(j.getString("sampleOriginTissue"));
                mfq.setSampleSampleSite(j.getString("sampleSampleSite"));
                mfq.setSampleExtractionMethod(j.getString("sampleExtractionMethod"));
                mfq.setSampleClassification(j.getString("sampleClassification"));
                mfq.setSampleTumorType(j.getString("sampleTumorType"));
                mfq.setDiagnosis(j.getString("diagnosis"));
                mfq.setMappedOntologyTerm(j.getString("mappedOntologyTerm"));
                mfq.setTreatmentHistory(j.getString("treatmentHistory"));


                JSONArray ja = j.getJSONArray("cancerSystem");
                List<String> cancerSystem = new ArrayList<>();
                for (int k = 0; k < ja.length(); k++) {

                    cancerSystem.add(ja.getString(k));
                }

                mfq.setCancerSystem(cancerSystem);

                ja = j.getJSONArray("allOntologyTermAncestors");
                Set<String> ancestors = new HashSet<>();

                for (int k = 0; k < ja.length(); k++) {

                    ancestors.add(ja.getString(k));
                }

                mfq.setAllOntologyTermAncestors(ancestors);

                if(j.has("dataAvailable")){
                    ja = j.getJSONArray("dataAvailable");
                    List<String> dataAvailable = new ArrayList<>();

                    for(int k=0; k<ja.length(); k++){

                        dataAvailable.add(ja.getString(k));
                    }

                    mfq.setDataAvailable(dataAvailable);
                }


                this.models.add(mfq);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    void initializeMutations(){

        log.info("Initializing mutations");

        String mut = dataProjectionRepository.findByLabel("PlatformMarkerVariantModel").getValue();

        JSONObject j;
        JSONArray ja;
        try{
            j = new JSONObject(mut);

            ObjectMapper mapper = new ObjectMapper();

            Map<String, Map<String, Map<String, Set<Long>>>> map = mapper.readValue(mut, new TypeReference<Map<String, Map<String, Map<String, Set<Long>>>>>(){});

            log.info("Lookup: "+map.get("TargetedNGS_MUT").get("RB1").get("N123D").toString());

            for(Map.Entry<String, Map<String, Map<String, Set<Long>>>> platformEntry: map.entrySet()){

                for(Map.Entry<String, Map<String,Set<Long>>> markerEntry: platformEntry.getValue().entrySet()){

                    for(Map.Entry<String, Set<Long>> variationEntry: markerEntry.getValue().entrySet()){

                        String marker = markerEntry.getKey();
                        String variant = variationEntry.getKey();
                        Set models = variationEntry.getValue();

                        addModelsToMutations(marker, variant, models);

                    }
                }
            }
        }
        catch(Exception e){

            e.printStackTrace();
        }

    }


    private void addModelsToMutations(String marker, String variant, Set<Long> models){

        if(mutations.containsKey(marker)){

            if(mutations.get(marker).containsKey(variant)){

                for(Long model: models){

                    mutations.get(marker).get(variant).add(model);
                }

            }
            //marker found, new variant
            else{
                Set<Long> newModelSet = new HashSet<>();

                for(Long model: models){

                    newModelSet.add(model);
                }

                mutations.get(marker).put(variant, newModelSet);
            }
        }
        //new marker
        else{

            Set<Long> newModelSet = new HashSet<>();

            for(Long model: models){

                newModelSet.add(model);
            }

            Map<String, Set<Long>> newVariant = new HashMap();

            newVariant.put(variant, newModelSet);

            mutations.put(marker, newVariant);
        }
    }


    private Set<Long> getModelsByMutatedMarkerAndVariant(String marker, String variant){

        if(mutations.containsKey(marker)){

            if(mutations.get(marker).containsKey(variant)){

                return mutations.get(marker).get(variant);
            }
        }

        return null;
    }


    /**
     * Search function accespts a Map of key value pairs
     * key = what facet to search
     * list of values = what values to filter on (using OR)
     * <p>
     * EX of expected data structure:
     * <p>
     * patient_age -> { 5-10, 20-40 },
     * patient_gender -> { Male },
     * sample_origin_tissue -> { Lung, Liver }
     * <p>
     * would yield results for male patients between 5-10 OR between 20-40 AND that had cancers in the lung OR liver
     *
     * @param filters
     * @return set of models derived from filtering the complete set according to the
     * filters passed in as arguments
     */
    public Set<ModelForQuery> search(Map<SearchFacetName, List<String>> filters) {

        Set<ModelForQuery> result = new HashSet<>(models);

        // If no filters have been specified, return the complete set
        if (filters == null) {
            return result;
        }

        for (SearchFacetName facet : filters.keySet()) {

            Predicate predicate;

            switch (facet) {

                case query:

                    predicate = getContainsMatchDisjunctionPredicate(filters.get(SearchFacetName.query));

                    Set<ModelForQuery> accumulate = new HashSet<>();
                    for (ModelForQuery r : result) {

                        Set<String> i = r.getAllOntologyTermAncestors().stream().filter(x -> predicate.test(x)).collect(Collectors.toSet());
                        if (i != null && i.size() > 0) {
                            r.setQueryMatch(i);
                            accumulate.add(r);
                        }

                    }

                    result = accumulate;
                    break;

                case datasource:

                    predicate = getExactMatchDisjunctionPredicate(filters.get(SearchFacetName.datasource));
                    result = result.stream().filter(x -> predicate.test(x.getDatasource())).collect(Collectors.toSet());
                    break;

                case diagnosis:

                    predicate = getExactMatchDisjunctionPredicate(filters.get(SearchFacetName.diagnosis));
                    result = result.stream().filter(x -> predicate.test(x.getMappedOntologyTerm())).collect(Collectors.toSet());
                    break;

                case patient_age:

                    predicate = getExactMatchDisjunctionPredicate(filters.get(SearchFacetName.patient_age));
                    result = result.stream().filter(x -> predicate.test(x.getPatientAge())).collect(Collectors.toSet());
                    break;

                case patient_treatment_status:

                    predicate = getExactMatchDisjunctionPredicate(filters.get(SearchFacetName.patient_treatment_status));
                    result = result.stream().filter(x -> predicate.test(x.getPatientTreatmentStatus())).collect(Collectors.toSet());
                    break;

                case patient_gender:

                    predicate = getExactMatchDisjunctionPredicate(filters.get(SearchFacetName.patient_gender));
                    result = result.stream().filter(x -> predicate.test(x.getPatientGender())).collect(Collectors.toSet());
                    break;

                case sample_origin_tissue:

                    predicate = getExactMatchDisjunctionPredicate(filters.get(SearchFacetName.sample_origin_tissue));
                    result = result.stream().filter(x -> predicate.test(x.getSampleOriginTissue())).collect(Collectors.toSet());
                    break;

                case sample_classification:

                    predicate = getExactMatchDisjunctionPredicate(filters.get(SearchFacetName.sample_classification));
                    result = result.stream().filter(x -> predicate.test(x.getSampleClassification())).collect(Collectors.toSet());
                    break;

                case sample_tumor_type:

                    predicate = getExactMatchDisjunctionPredicate(filters.get(SearchFacetName.sample_tumor_type));
                    result = result.stream().filter(x -> predicate.test(x.getSampleTumorType())).collect(Collectors.toSet());
                    break;

                case model_implantation_site:

                    predicate = getExactMatchDisjunctionPredicate(filters.get(SearchFacetName.model_implantation_site));
                    result = result.stream().filter(x -> predicate.test(x.getModelImplantationSite())).collect(Collectors.toSet());
                    break;

                case model_implantation_type:

                    predicate = getExactMatchDisjunctionPredicate(filters.get(SearchFacetName.model_implantation_type));
                    result = result.stream().filter(x -> predicate.test(x.getModelImplantationType())).collect(Collectors.toSet());
                    break;

                case model_host_strain:

                    predicate = getExactMatchDisjunctionPredicate(filters.get(SearchFacetName.model_host_strain));
                    result = result.stream().filter(x -> predicate.test(x.getModelHostStrain())).collect(Collectors.toSet());
                    break;

                case cancer_system:

                    Set<ModelForQuery> toRemove = new HashSet<>();
                    for (ModelForQuery res : result) {
                        Boolean keep = Boolean.FALSE;
                        for (String s : filters.get(SearchFacetName.cancer_system)) {
                            if (res.getCancerSystem().contains(s)) {
                                keep = Boolean.TRUE;
                            }
                        }
                        if (!keep) {
                            toRemove.add(res);
                        }
                    }

                    result.removeAll(toRemove);
                    break;

                case organ:

                    predicate = getExactMatchDisjunctionPredicate(filters.get(SearchFacetName.organ));
                    result = result.stream().filter(x -> predicate.test(x.getCancerOrgan())).collect(Collectors.toSet());
                    break;

                case cell_type:

                    predicate = getExactMatchDisjunctionPredicate(filters.get(SearchFacetName.cell_type));
                    result = result.stream().filter(x -> predicate.test(x.getCancerCellType())).collect(Collectors.toSet());
                    break;

                case mutation:
                    // mutation=KRAS___MUT___V600E, mutation=NRAS___WT
                    // if the String has ___ (three underscores) twice, it is mutated, if it has only one, it is WT

                    Set<Long> modelsWithMutatedMarkerAndVariant = new HashSet<>();

                    for(String mutation: filters.get(SearchFacetName.mutation)){

                        if(mutation.split("___").length == 3){

                            String[] mut = mutation.split("___");
                            String marker = mut[0];
                            String variant = mut[2];
                            modelsWithMutatedMarkerAndVariant.addAll(getModelsByMutatedMarkerAndVariant(marker, variant));

                        }
                    }

                    result = result.stream().filter(x -> modelsWithMutatedMarkerAndVariant.contains(x.getModelId())).collect(Collectors.toSet());




                default:
                    // default case is an unexpected filter option
                    // Do not filter anything
                    log.info("Unrecognised facet {} passed to search, skipping.", facet);
                    break;
            }
        }

        return result;
    }


    /**
     * getExactMatchDisjunctionPredicate returns a composed predicate with all the supplied filters "OR"ed together
     * using an exact match
     * <p>
     * NOTE: This is a case sensitive match!
     *
     * @param filters the set of strings to match against
     * @return a composed predicate case insensitive matching the supplied filters using disjunction (OR)
     */
    Predicate<String> getExactMatchDisjunctionPredicate(List<String> filters) {
        List<Predicate<String>> preds = new ArrayList<>();

        // Iterate through the filter options passed in for this facet
        for (String filter : filters) {

            // Create a filter predicate for each option
            Predicate<String> pred = s -> s.equals(filter);

            // Store all filter options in a list
            preds.add(pred);
        }

        // Create a "combination" predicate containing sub-predicates "OR"ed together
        return preds.stream().reduce(Predicate::or).orElse(x -> false);
    }

    /**
     * getContainsMatchDisjunctionPredicate returns a composed predicate with all the supplied filters "OR"ed together
     * using a contains match
     * <p>
     * NOTE: This is a case insensitive match!
     *
     * @param filters the set of strings to match against
     * @return a composed predicate case insensitive matching the supplied filters using disjunction (OR)
     */
    Predicate getContainsMatchDisjunctionPredicate(List<String> filters) {
        List<Predicate<String>> preds = new ArrayList<>();

        // Iterate through the filter options passed in for this facet
        for (String filter : filters) {

            // Create a filter predicate for each option
            Predicate<String> pred = s -> s.toLowerCase().contains(filter.toLowerCase());

            // Store all filter options in a list
            preds.add(pred);
        }

        // Create a "combination" predicate containing sub-predicates "OR"ed together
        return preds.stream().reduce(Predicate::or).orElse(x -> false);
    }


    /**
     * Get the count of models for a supplied facet.
     * <p>
     * This method will return counts of facet options for the supplied facet
     *
     * @param facet    the facet to count
     * @param results  set of models already filtered
     * @param selected what facets have been filtered already
     * @return a list of {@link FacetOption} indicating counts and selected state
     */
    @Cacheable("facet_counts")
    public List<FacetOption> getFacetOptions(SearchFacetName facet, List<String> options, Set<ModelForQuery> results, List<String> selected) {

        Set<ModelForQuery> allResults = models;

        List<FacetOption> map = new ArrayList<>();

        // Initialise all facet option counts to 0 and set selected attribute on all options that the user has chosen
        if (options != null) {
            for (String option : options) {
                Long count = allResults
                        .stream()
                        .filter(x ->
                                Stream.of(x.getBy(facet).split("::"))
                                        .collect(Collectors.toSet())
                                        .contains(option))
                        .count();
                map.add(new FacetOption(option, selected != null ? 0 : count.intValue(), count.intValue(), selected != null && selected.contains(option) ? Boolean.TRUE : Boolean.FALSE, facet));
            }
        }

        // We want the counts on the facets to look something like:
        // Gender
        //   [ ] Male (1005 of 1005)
        //   [ ] Female (840 of 840)
        //   [ ] Not specified (31 of 31)
        // Then when a facet is clicked:
        // Gender
        //   [X] Male (1005 of (1005)
        //   [ ] Female (0 of 840)
        //   [ ] Not specified (2 of 31)
        //


        // Iterate through results adding count to the appropriate option
        for (ModelForQuery mfq : results) {

            String s = mfq.getBy(facet);

            // Skip empty facets
            if (s == null || s.equals("")) {
                continue;
            }

            // List of ontology terms may come from the service.  These will by separated by "::" delimiter
            if (s.contains("::")) {

                for (String ss : s.split("::")) {

                    // There should be only one element per facet name
                    map.forEach(x -> {
                        if (x.getName().equals(ss)) {
                            x.increment();
                        }
                    });
                }

            } else {

                // Initialise on the first time we see this facet name
                if (map.stream().noneMatch(x -> x.getName().equals(s))) {
                    map.add(new FacetOption(s, 0, 0, selected != null && selected.contains(s) ? Boolean.TRUE : Boolean.FALSE, facet));
                }

                // There should be only one element per facet name
                map.forEach(x -> {
                    if (x.getName().equals(s)) {
                        x.increment();
                    }
                });
            }
        }


//        Collections.sort(map);

        return map;
    }


}