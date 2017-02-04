package org.pdxfinder.repositories;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pdxfinder.TestConfig;
import org.pdxfinder.dao.ExternalDataSource;
import org.pdxfinder.dao.Tissue;
import org.pdxfinder.dao.Tumor;
import org.pdxfinder.dao.TumorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.Instant;

/**
 * Testing repository for managing tumor objects
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@TestPropertySource(locations = {"classpath:ogm.properties"})
@SpringBootTest
@Transactional
public class TumorRepositoryTest {

    private final static Logger log = LoggerFactory.getLogger(TumorRepositoryTest.class);
    private String tumorTypeName = "TEST_TUMORTYPE";
    private String extDsName = "TEST_SOURCE";
    private String tissueName = "TEST_TISSUE";

    @Autowired
    private TumorRepository tumorRepository;

    @Autowired
    private TissueRepository tissueRepository;

    @Autowired
    private TumorTypeRepository tumorTypeRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ExternalDataSourceRepository externalDataSourceRepository;

    @Before
    public void cleanDb() {

        tumorRepository.deleteAll();
        patientRepository.deleteAll();
        externalDataSourceRepository.deleteAll();
        tissueRepository.deleteAll();

        TumorType tumorType = tumorTypeRepository.findByName(tumorTypeName);
        if (tumorType == null) {
            log.info("Tumor type ", tumorTypeName, "not found. Creating");
            tumorType = new TumorType(tumorTypeName);
            tumorTypeRepository.save(tumorType);
        }

        ExternalDataSource ds = externalDataSourceRepository.findByName(extDsName);
        if (ds == null) {
            log.info("External data source ", extDsName, "not found. Creating");
            ds = new ExternalDataSource(extDsName, extDsName, extDsName, Date.from(Instant.now()));
            externalDataSourceRepository.save(ds);
        }

        Tissue tissue = tissueRepository.findByName(tissueName);
        if (tissue == null) {
            log.info("Tissue ", extDsName, "not found. Creating");
            tissue = new Tissue(tissueName);
            tissueRepository.save(tissue);
        }

    }

    @Test
    public void createTumorInGraphDb() throws Exception {

        // Tumor:
        //   String sourceTumorId
        //   TumorType type
        //   String diagnosis
        //   Tissue originTissue
        //   Tissue tumorSite
        //   String classification
        //   ExternalDataSource externalDataSource

        TumorType tumorType = tumorTypeRepository.findByName(tumorTypeName);
        Tissue tissue = tissueRepository.findByName(tissueName);
        ExternalDataSource externalDataSource = externalDataSourceRepository.findByAbbreviation(extDsName);

        String TEST_TUMOR_ID = "TESTID-1";
        generateTumor(tumorType, tissue, externalDataSource, TEST_TUMOR_ID);

        Tumor foundTumor = tumorRepository.findBySourceTumorId(TEST_TUMOR_ID);
        Assert.assertNotNull(foundTumor);


    }

    @Test
    public void deleteTumor() throws Exception {

        TumorType tumorType = tumorTypeRepository.findByName(tumorTypeName);
        Tissue tissue = tissueRepository.findByName(tissueName);
        ExternalDataSource externalDataSource = externalDataSourceRepository.findByAbbreviation(extDsName);

        String testTumorId = "DELETE_TESTID-1";
        generateTumor(tumorType, tissue, externalDataSource, testTumorId);

        Tumor foundTumor = tumorRepository.findBySourceTumorId(testTumorId);
        Assert.assertNotNull(foundTumor);

        tumorRepository.delete(foundTumor);
        foundTumor = tumorRepository.findBySourceTumorId(testTumorId);
        Assert.assertNull(foundTumor);
    }


    @Test
    public void deleteMultipleTumorsByDatasource() throws Exception {

        TumorType tumorType = tumorTypeRepository.findByName(tumorTypeName);
        Tissue tissue = tissueRepository.findByName(tissueName);
        ExternalDataSource externalDataSource = externalDataSourceRepository.findByAbbreviation(extDsName);

        for (Integer i = 0; i < 20; i++) {
            String testTumorId = "TESTID-" + i;
            generateTumor(tumorType, tissue, externalDataSource, testTumorId);
        }

        Tumor foundTumor = tumorRepository.findBySourceTumorId("TESTID-12");
        Assert.assertNotNull(foundTumor);

        // Added 20 tumors, count should be 20
        Assert.assertEquals(20, tumorRepository.count());

        // Delete all tumors, count should be 0
        tumorRepository.delete(tumorRepository.findByExternalDataSourceAbbreviation(externalDataSource.getAbbreviation()));
        Assert.assertEquals(0, tumorRepository.count());

    }

    private void generateTumor(TumorType tumorType, Tissue tissue, ExternalDataSource externalDataSource, String TEST_TUMOR_ID) {
        Tumor tumor = new Tumor(TEST_TUMOR_ID, tumorType, "TEST_DIAGNOSIS", tissue, tissue, "TEST_CLASSIFICATION", externalDataSource);
        tumorRepository.save(tumor);
    }


}
