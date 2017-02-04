package org.pdxfinder.repositories;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.pdxfinder.TestConfig;
import org.pdxfinder.dao.ExternalDataSource;
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

import java.sql.Date;
import java.time.Instant;

/**
 * Test suite for external data source repository management
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@TestPropertySource(locations = {"classpath:ogm.properties"})
@SpringBootTest
@Transactional
public class ExternalDataSourceRepositoryTest {

    private final static Logger log = LoggerFactory.getLogger(PatientRepositoryTest.class);
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
            log.info("External data source ", extDsName, "not found. Creating");
            ds = new ExternalDataSource(extDsName, extDsName, extDsName, Date.from(Instant.now()));
            externalDataSourceRepository.save(ds);
        }

        Assert.notNull(externalDataSourceRepository.findByName(extDsName));
    }

}