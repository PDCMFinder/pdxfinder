package org.pdxfinder.services;

import org.junit.Before;
import org.junit.Test;
import org.pdxfinder.BaseTest;
import org.pdxfinder.repositories.SampleRepository;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.util.Assert;

/**
 * Created by jmason on 06/07/2017.
 */
public class SearchServiceTest extends BaseTest {

    @MockBean
    private SampleRepository sampleRepository;


    @Before
    private void setup() {
        // Preload the base data in the graph for testing the service

    }

    @Test
    public void searchForBreastCancerModels() throws Exception {

        Assert.notNull(sampleRepository, "Sample repo is null");

    }


}