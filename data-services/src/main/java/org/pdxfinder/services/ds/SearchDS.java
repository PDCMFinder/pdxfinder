package org.pdxfinder.services.ds;

import org.pdxfinder.dao.ModelCreation;
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

    /**
     * Populate the complete set of models for searching when this object is instantiated
     */
    public SearchDS(ModelCreationRepository modelCreationRepository) {
        Assert.notNull(modelCreationRepository, "Model repository cannot be null");
        this.models = new HashSet<>();

        // When this class is instantiated, populate and cache the models set

        for (ModelCreation mc : modelCreationRepository.getModelsWithPatientData()) {

            ModelForQuery mfq = new ModelForQuery();
            mfq.setModelId(mc.getId());
            mfq.setDatasource(mc.getDataSource());

            // Patient information
            mfq.setPatientAge(mc.getSample().getPatientSnapshot().getAgeBin());
            mfq.setPatientGender(mc.getSample().getPatientSnapshot().getPatient().getSex());

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

            // TODO : complete the options etc.

            models.add(mfq);
        }
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

                    predicate = getExactMatchDisjunctionPredicate(filters.get(SearchFacetName.system));
                    result = result.stream().filter(x -> predicate.test(x.getCancerSystem())).collect(Collectors.toSet());
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
