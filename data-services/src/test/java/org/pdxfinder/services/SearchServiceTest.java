package org.pdxfinder.services;

import org.junit.Before;
import org.junit.Test;
import org.pdxfinder.BaseTest;
import org.pdxfinder.graph.repositories.SampleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.util.Assert;

/**
 * Created by jmason on 06/07/2017.
 */
public class SearchServiceTest extends BaseTest {

    private final static Logger log = LoggerFactory.getLogger(SearchServiceTest.class);

    @MockBean
    private SampleRepository sampleRepository;

    @Before
    public void setup() {
        // Preload the base data in the graph for testing the service
        log.info("Set up");
    }

    @Test
    public void searchForBreastCancerModels() throws Exception {

        log.info("Testing mock sample repository");
        Assert.notNull(sampleRepository, "Sample repo is null");

    }

    @Test
    public void anotherTest() {
        log.info("This is another test");
    }

}