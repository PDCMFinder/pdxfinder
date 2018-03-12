package org.pdxfinder.services.ds;

import org.junit.Before;
import org.junit.Test;
import org.pdxfinder.BaseTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SearchDSTest extends BaseTest {

    private final static Logger log = LoggerFactory.getLogger(SearchDSTest.class);

    /*

    @Autowired
    SearchDS searchDS;

    private Set<ModelForQuery> models = new HashSet<>();

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

        assertThat(models.size(), is(4));
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

*/
}