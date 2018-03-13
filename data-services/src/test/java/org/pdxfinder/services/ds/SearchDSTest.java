package org.pdxfinder.services.ds;

import org.junit.Before;
import org.junit.Test;
import org.pdxfinder.BaseTest;
import org.pdxfinder.dao.DataProjection;
import org.pdxfinder.repositories.DataProjectionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.crypto.Data;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SearchDSTest extends BaseTest {

    private final static Logger log = LoggerFactory.getLogger(SearchDSTest.class);

    SearchDS searchDS;
    private Set<ModelForQuery> models = new HashSet<>();

    @Autowired
    DataProjectionRepository dataProjectionRepository;

    @Before
    public void setUp() {

        ModelForQuery m = new ModelForQuery();
        m.setModelId(1L);
        m.setPatientAge("TEST FILTER");
        m.setPatientGender("Male");
        models.add(m);

        m = new ModelForQuery();
        m.setModelId(2L);
        m.setPatientAge("TEST FILTER 2");
        m.setPatientGender("Female");
        models.add(m);

        m = new ModelForQuery();
        m.setModelId(3L);
        m.setPatientAge("TEST FILTER 3");
        m.setPatientGender("Male");
        models.add(m);

        m = new ModelForQuery();
        m.setModelId(4L);
        m.setPatientAge("TEST FILTER 4");
        m.setPatientGender("Male");
        models.add(m);

        DataProjection dp = new DataProjection();
        dp.setLabel("ModelForQuery");
        dp.setValue("[{\"modelId\":164005,\"datasource\":\"PDXNet-WUSTL\",\"externalId\":\"WUSTL 911-06\",\"patientAge\":\"30-39\",\"patientGender\":\"Female\",\"sampleOriginTissue\":\"Not Specified\",\"sampleSampleSite\":\"Not Specified\",\"sampleExtractionMethod\":\"surgical sample\",\"sampleClassification\":\"ypT3/N1b/M1a/G1/G2\",\"sampleTumorType\":\"Primary\",\"cancerSystem\":[\"Unclassified\"],\"diagnosis\":\"Adenocarcinoma\",\"mappedOntologyTerm\":\"Adenocarcinoma\",\"treatmentHistory\":\"Not Specified\",\"allOntologyTermAncestors\":[\"Adenocarcinoma\",\"Carcinoma\",\"Epithelial Neoplasm\",\"Cancer\",\"Glandular Cell Neoplasm\",\"Neoplasm by Morphology\"]}]");

        DataProjection mutDP = new DataProjection();
        mutDP.setLabel("PlatformMarkerVariantModel");
        mutDP.setValue("{\"TargetedNGS_MUT\":{\"RB1\":{\"N123D\":[10411],\"Q383E\":[10940],\"E323Q\":[16519],\"G38S\":[12539]}}}");

        dataProjectionRepository.save(dp);
        dataProjectionRepository.save(mutDP);

        assertThat(models.size(), is(4));

        this.searchDS = new SearchDS(dataProjectionRepository);
        searchDS.initialize();

    }

    @Test
    public void getExactMatchDisjunctionPredicateTest() {

        List<String> filters = Arrays.asList("TEST FILTER", "TEST FILTER 2");
        Predicate pred = searchDS.getExactMatchDisjunctionPredicate(filters);

        Set<ModelForQuery> results = new HashSet<>(models);
        Set<ModelForQuery> filteredResults = results.stream().filter(x -> pred.test(x.getPatientAge())).collect(Collectors.toSet());
        assertThat(filteredResults.size(), is(2));

    }

    @Test
    public void testCombinedFilters() {

        List<String> patientAgefilters = Arrays.asList("TEST FILTER", "TEST FILTER 2");
        Predicate predAge = searchDS.getExactMatchDisjunctionPredicate(patientAgefilters);

        List<String> patientGenderfilters = Arrays.asList("Female");
        Predicate predGender = searchDS.getExactMatchDisjunctionPredicate(patientGenderfilters);


        Set<ModelForQuery> results = new HashSet<>(models);

        Set<ModelForQuery> filteredResults = results.stream().filter(x -> predAge.test(x.getPatientAge())).collect(Collectors.toSet());
        assertThat(filteredResults.size(), is(2));

        filteredResults = filteredResults.stream().filter(x -> predGender.test(x.getPatientGender())).collect(Collectors.toSet());
        assertThat(filteredResults.size(), is(1));


        // Test applying the filters in another order to ensure the same result comes a the end

        filteredResults = results.stream().filter(x -> predGender.test(x.getPatientGender())).collect(Collectors.toSet());
        assertThat(filteredResults.size(), is(1));

        filteredResults = filteredResults.stream().filter(x -> predAge.test(x.getPatientAge())).collect(Collectors.toSet());
        assertThat(filteredResults.size(), is(1));

        assertThat(new ArrayList<>(filteredResults).get(0).getModelId(), is(2L));

        log.info("Result after filtering is ", filteredResults);


    }


}