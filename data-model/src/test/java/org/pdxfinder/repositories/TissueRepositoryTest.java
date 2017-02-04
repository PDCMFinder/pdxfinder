package org.pdxfinder.repositories;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pdxfinder.TestConfig;
import org.pdxfinder.dao.Tissue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * Test suite for tissue repository management
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@TestPropertySource(locations = {"classpath:ogm.properties"})
@SpringBootTest
@Transactional
public class TissueRepositoryTest {

    private final static Logger log = LoggerFactory.getLogger(TissueRepositoryTest.class);
    private String tissueName = "TEST_TISSUE";

    @Autowired
    private TissueRepository tissueRepository;

    @Before
    public void setupDb() {
        tissueRepository.deleteAll();
    }

    @BeforeTransaction
    @Rollback(false)
    public void setupTransaction() {

    }


    @Test
    public void createTissue() {

        Tissue tissue = tissueRepository.findByName(tissueName);
        if (tissue == null) {
            log.info("Tissue {} not found. Creating", tissueName);
            tissue = new Tissue(tissueName);
            tissueRepository.save(tissue);
        }

        Assert.notNull(tissueRepository.findByName(tissueName));
    }

}