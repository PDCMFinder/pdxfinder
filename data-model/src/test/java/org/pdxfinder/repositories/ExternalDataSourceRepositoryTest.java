package org.pdxfinder.repositories;

import org.junit.Assert;
import org.junit.Test;
import org.pdxfinder.BaseTest;
import org.pdxfinder.dao.ExternalDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.transaction.BeforeTransaction;

import java.sql.Date;
import java.time.Instant;

/**
 * Test suite for external data source repository management
 */
public class ExternalDataSourceRepositoryTest extends BaseTest {

    private final static Logger log = LoggerFactory.getLogger(ExternalDataSourceRepositoryTest.class);
    private String extDsName = "TEST_SOURCE";

    @Autowired
    private ExternalDataSourceRepository externalDataSourceRepository;

    @BeforeTransaction
    @Rollback(false)
    public void setupDb() {

    }

    @Test
    public void createDatasource() {
        externalDataSourceRepository.deleteAll();

        ExternalDataSource ds = externalDataSourceRepository.findByName(extDsName);
        if (ds == null) {
            log.debug("External data source {} not found. Creating", extDsName);
            ds = new ExternalDataSource(extDsName, extDsName, extDsName, extDsName, Date.from(Instant.now()));
            externalDataSourceRepository.save(ds);
        }

        Assert.assertNotNull(externalDataSourceRepository.findByName(extDsName));
    }

}