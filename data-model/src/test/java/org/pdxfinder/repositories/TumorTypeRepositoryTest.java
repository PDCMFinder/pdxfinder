package org.pdxfinder.repositories;

import org.junit.Test;
import org.pdxfinder.BaseTest;
import org.pdxfinder.dao.TumorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.transaction.BeforeTransaction;

import java.util.Arrays;
import java.util.List;

/**
 * Tests for the Tumor Type repository
 */
public class TumorTypeRepositoryTest extends BaseTest {

    private final static Logger log = LoggerFactory.getLogger(TumorTypeRepositoryTest.class);

    @Autowired
    private
    TumorTypeRepository tumorTypeRepository;

    @Rollback(false)
    @BeforeTransaction
    public void cleanDb() {
        tumorTypeRepository.deleteAll();
    }

    @Test
    public void createTumorTypesInGraphDb() throws Exception {

        List<String> types = Arrays.asList("Metastasis", "Metastatic", "Not Specified", "Primary Malignancy", "Recurrent/Relapse");

        for (String type : types) {
            TumorType foundType = tumorTypeRepository.findByName(type);
            if (foundType == null) {
                log.debug("Tumor type {} not found. Creating", type);
                foundType = new TumorType(type);
                tumorTypeRepository.save(foundType);
            }

            foundType = tumorTypeRepository.findByName(type);
            log.info("Found Tumor type {}", foundType.getName());

            assert (foundType.getName().equals(type));

        }

    }

}