package org.pdxfinder.repositories;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pdxfinder.BaseTest;
import org.pdxfinder.dao.ExternalDataSource;
import org.pdxfinder.dao.Sample;
import org.pdxfinder.dao.Tissue;
import org.pdxfinder.dao.TumorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Date;
import java.time.Instant;

/**
 * Testing repository for managing tumor objects
 */
public class SampleRepositoryTest extends BaseTest {

    private final static Logger log = LoggerFactory.getLogger(SampleRepositoryTest.class);
    private String tumorTypeName = "TEST_TUMORTYPE";
    private String extDsName = "TEST_SOURCE";
    private String tissueName = "TEST_TISSUE";

    @Autowired
    private SampleRepository sampleRepository;

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

        sampleRepository.deleteAll();
        patientRepository.deleteAll();
        externalDataSourceRepository.deleteAll();
        tissueRepository.deleteAll();

        TumorType tumorType = tumorTypeRepository.findByName(tumorTypeName);
        if (tumorType == null) {
            log.debug("Sample type {} not found. Creating", tumorTypeName);
            tumorType = new TumorType(tumorTypeName);
            tumorTypeRepository.save(tumorType);
        }

        ExternalDataSource ds = externalDataSourceRepository.findByName(extDsName);
        if (ds == null) {
            log.debug("External data source {} not found. Creating", extDsName);
            ds = new ExternalDataSource(extDsName, extDsName, extDsName, extDsName, Date.from(Instant.now()));
            externalDataSourceRepository.save(ds);
        }

        Tissue tissue = tissueRepository.findByName(tissueName);
        if (tissue == null) {
            log.debug("Tissue {} not found. Creating", extDsName);
            tissue = new Tissue(tissueName);
            tissueRepository.save(tissue);
        }

    }

    @Test
    public void createTumorInGraphDb() throws Exception {

        // Sample:
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

        Sample foundSample = sampleRepository.findBySourceSampleIdAndDataSource(TEST_TUMOR_ID, externalDataSource.getAbbreviation());
        Assert.assertNotNull(foundSample);


    }

    @Test
    public void deleteTumor() throws Exception {

        TumorType tumorType = tumorTypeRepository.findByName(tumorTypeName);
        Tissue tissue = tissueRepository.findByName(tissueName);
        ExternalDataSource externalDataSource = externalDataSourceRepository.findByAbbreviation(extDsName);

        String testTumorId = "DELETE_TESTID-1";
        generateTumor(tumorType, tissue, externalDataSource, testTumorId);

        Sample foundSample = sampleRepository.findBySourceSampleIdAndDataSource(testTumorId, externalDataSource.getAbbreviation());
        Assert.assertNotNull(foundSample);

        sampleRepository.delete(foundSample);
        foundSample = sampleRepository.findBySourceSampleIdAndDataSource(testTumorId, externalDataSource.getAbbreviation());
        Assert.assertNull(foundSample);
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

        Sample foundSample = sampleRepository.findBySourceSampleIdAndDataSource("TESTID-12", externalDataSource.getAbbreviation());
        Assert.assertNotNull(foundSample);

        // Added 20 tumors, count should be 20
        Assert.assertEquals(20, sampleRepository.count());


    }
//String sourceSampleId, TumorType type, String diagnosis, Tissue originTissue, Tissue sampleSite, String extractionMethod, String classification, Boolean normalTissue
    private void generateTumor(TumorType tumorType, Tissue tissue, ExternalDataSource externalDataSource, String TEST_TUMOR_ID) {
        Sample sample = new Sample(TEST_TUMOR_ID, tumorType, "TEST_DIAGNOSIS", tissue, tissue, "Surgical Resection", "TEST_CLASSIFICATION", false, externalDataSource.getAbbreviation());
        sampleRepository.save(sample);
    }


}
