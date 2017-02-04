package org.pdxfinder.repositories;

import org.junit.Before;
import org.junit.Test;
import org.pdxfinder.BaseTest;
import org.pdxfinder.dao.Tissue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.util.Assert;

/**
 * Test suite for tissue repository management
 */
public class TissueRepositoryTest extends BaseTest {

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
            log.debug("Tissue {} not found. Creating", tissueName);
            tissue = new Tissue(tissueName);
            tissueRepository.save(tissue);
        }

        Assert.notNull(tissueRepository.findByName(tissueName));
    }

}