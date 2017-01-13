package org.pdxi.repositories;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.pdxi.TestConfig;
import org.pdxi.dao.Patient;
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

/**
 * Created by jmason on 09/01/2017.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@TestPropertySource(locations = {"classpath:ogm.properties"})
@Transactional
@SpringBootTest
public class PatientRepositoryTest {

    private final static Logger log = LoggerFactory.getLogger(PatientRepositoryTest.class);

    @Autowired
    PatientRepository patientRepository;

    @Rollback(false)
    @BeforeTransaction
    public void cleanDb() {
        patientRepository.deleteAll();
    }

    @Test
    public void persistedPatientShouldBeRetrievableFromGraphDb() throws Exception {

        Patient femalePatient = new Patient("-9999", "F", "65", null, null);
        patientRepository.save(femalePatient);

        Patient foundFemalePatient = patientRepository.findBySexAndAge("F", "65").iterator().next();
        assert (foundFemalePatient != null);
        assert (foundFemalePatient.getSex().equals("F"));
        assert (foundFemalePatient.getAge().equals("65"));

        log.info(foundFemalePatient.toString());

//        patientRepository.delete(patientRepository.findByExternalId("-9999"));


    }

}