package org.pdxfinder.services.ds;

import org.pdxfinder.dao.ModelCreation;
import org.pdxfinder.dao.OntologyTerm;
import org.pdxfinder.dao.Specimen;
import org.pdxfinder.repositories.ModelCreationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/*
 * Created by csaba on 19/01/2018.
 */

@Component
public class SearchDS {

    private final static Logger log = LoggerFactory.getLogger(SearchDS.class);

    private Set<ModelForQuery> models;
    private Map<String, String> cancerSystemMap = new HashMap<>();

    public static List<String> CANCERS_BY_SYSTEM = Arrays.asList(
            "Breast cancer",
            "Cardiovascular Cancer",
            "Connective and Soft Tissue cancer",
            "Digestive System Cancer",
            "Endocrine Cancer",
            "Eye Cancer",
            "Head and Neck Cancer",
            "Hematopoietic and Lymphoid System Cancer",
            "Nervous System Cancer",
            "Peritoneal and Retroperitoneal Cancer",
            "Malignant Reproductive System Cancer",
            "Respiratory Tract Cancer",
            "Thoracic cancer",
            "Skin cancer",
            "Urinary System Cancer",
            "Unclassified"
    );

    /**
     * Populate the complete set of models for searching when this object is instantiated
     */
    public SearchDS(ModelCreationRepository modelCreationRepository) {
        Assert.notNull(modelCreationRepository, "Model repository cannot be null");
        this.models = new HashSet<>();

        // Mapping NCIT ontology term labels to display labels
        this.cancerSystemMap.put("Malignant Breast Neoplasm", "Breast cancer");
        this.cancerSystemMap.put("Malignant Cardiovascular Neoplasm", "Cardiovascular Cancer");
        this.cancerSystemMap.put("Connective and Soft Tissue Neoplasm", "Connective and Soft Tissue cancer");
        this.cancerSystemMap.put("Malignant Digestive System Neoplasm", "Digestive System Cancer");
        this.cancerSystemMap.put("Malignant Endocrine Neoplasm", "Endocrine Cancer");
        this.cancerSystemMap.put("Malignant Eye Neoplasm", "Eye Cancer");
        this.cancerSystemMap.put("Malignant Head and Neck Neoplasm", "Head and Neck Cancer");
        this.cancerSystemMap.put("Hematopoietic and Lymphoid System Neoplasm", "Hematopoietic and Lymphoid System Cancer");
        this.cancerSystemMap.put("Malignant Nervous System Neoplasm", "Nervous System Cancer");
        this.cancerSystemMap.put("Peritoneal and Retroperitoneal Neoplasms", "Peritoneal and Retroperitoneal Cancer");
        this.cancerSystemMap.put("Malignant Reproductive System Neoplasm", "Malignant Reproductive System Cancer");
        this.cancerSystemMap.put("Malignant Respiratory Tract Neoplasm", "Respiratory Tract Cancer");
        this.cancerSystemMap.put("Thoracic Disorder", "Thoracic cancer");
        this.cancerSystemMap.put("Malignant Skin Neoplasm", "Skin cancer");
        this.cancerSystemMap.put("Malignant Urinary System Neoplasm", "Urinary System Cancer");
        this.cancerSystemMap.put("Unclassified", "Unclassified");




        // When this class is instantiated, populate and cache the models set

        for (ModelCreation mc : modelCreationRepository.getModelsWithPatientData()) {

            ModelForQuery mfq = new ModelForQuery();
            mfq.setModelId(mc.getId());
            mfq.setExternalId(mc.getSourcePdxId());
            mfq.setDatasource(mc.getDataSource());

            // Patient information
            mfq.setPatientAge(mc.getSample().getPatientSnapshot().getAgeBin());
            mfq.setPatientGender(mc.getSample().getPatientSnapshot().getPatient().getSex());
            mfq.setDiagnosis(mc.getSample().getDiagnosis());

            if (mc.getSample().getPatientSnapshot().getTreatmentNaive() != null) {
                mfq.setPatientTreatmentStatus(mc.getSample().getPatientSnapshot().getTreatmentNaive().toString());
            }

            // Sample information
            mfq.setSampleExtractionMethod(mc.getSample().getExtractionMethod());
            mfq.setSampleOriginTissue(mc.getSample().getOriginTissue().getName());
            mfq.setSampleClassification(mc.getSample().getClassification());

            if (mc.getSample().getType() != null) {
                mfq.setSampleTumorType(mc.getSample().getType().getName());
            }
            // Model information
            Set<Specimen> specimens = mc.getSpecimens();
            if (specimens != null && specimens.size() > 0) {

                Specimen s = specimens.iterator().next();
                mfq.setModelBackgroundStrain(s.getBackgroundStrain().getSymbol());
                mfq.setModelImplantationSite(s.getImplantationSite().getName());
                mfq.setModelImplantationType(s.getImplantationType().getName());
            }

            // Get all ancestor ontology terms into a set specific for this model
            Set<OntologyTerm> allOntologyTerms = new HashSet<>();
            for (OntologyTerm t : mc.getSample().getSampleToOntologyRelationShip().getOntologyTerm().getSubclassOf()) {
                allOntologyTerms.addAll(getAllAncestors(t));
            }

            // Add all top level systems (translated) to the Model
            for (String s : allOntologyTerms.stream().map(OntologyTerm::getLabel).collect(Collectors.toSet())) {

                if (this.cancerSystemMap.keySet().contains(s)) {

                    if (mfq.getCancerSystem() == null) {
                        mfq.setCancerSystem(new ArrayList<>());
                    }

                    mfq.getCancerSystem().add(this.cancerSystemMap.get(s));

                }
            }

            // Ensure that ALL models have a system -- even if it's not in the ontology nodes specified
            if (mfq.getCancerSystem() == null || mfq.getCancerSystem().size() == 0) {
                if (mfq.getCancerSystem() == null) {
                    mfq.setCancerSystem(new ArrayList<>());
                }

                mfq.getCancerSystem().add(this.cancerSystemMap.get("Unclassified"));

            }

            // TODO: Complete the organ options
            // TODO: Complete the cell type options
            // TODO: Complete the patient treatment options


            models.add(mfq);
        }

//        try (FileOutputStream fout = new FileOutputStream("/models.ser"); ObjectOutputStream oos = new ObjectOutputStream(fout)) {
//            oos.writeObject(models);
//        } catch (IOException e) {
//            log.warn("Cannot serialize models to file, startup times will be slow", e);
//        }

    }

    /**
     * Recursively get all ancestors starting from the supplied ontology term
     *
     * @param t the starting term in the ontology
     * @return a set of ontology terms corresponding to the ancestors of the term supplied
     */
    public Set<OntologyTerm> getAllAncestors(OntologyTerm t) {

        Set<OntologyTerm> retSet = new HashSet<>();

        if (t.getSubclassOf() == null || t.getSubclassOf().size() == 0) {
            return null;
        }


        for (OntologyTerm st : t.getSubclassOf()) {

            retSet.add(st);
            Set<OntologyTerm> intSet = getAllAncestors(st);
            if (intSet != null) retSet.addAll(intSet);
        }

        return retSet;
    }

    public Set<ModelForQuery> getModels() {
        return models;
    }

    public void setModels(Set<ModelForQuery> models) {
        this.models = models;
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
            List<Predicate<ModelForQuery>> preds = new ArrayList<>();
            Predicate predicate;
            switch (facet) {

                case datasource:

                    predicate = getExactMatchDisjunctionPredicate(filters.get(SearchFacetName.datasource));
                    result = result.stream().filter(x -> predicate.test(x.getDatasource())).collect(Collectors.toSet());
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

                case model_background_strain:

                    predicate = getExactMatchDisjunctionPredicate(filters.get(SearchFacetName.model_background_strain));
                    result = result.stream().filter(x -> predicate.test(x.getModelBackgroundStrain())).collect(Collectors.toSet());
                    break;

                case system:

                    Set<ModelForQuery> toRemove = new HashSet<>();
                    for (ModelForQuery res : result) {
                        Boolean keep = Boolean.FALSE;
                        for (String s : filters.get(SearchFacetName.system)) {
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


}
