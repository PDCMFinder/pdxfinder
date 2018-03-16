package org.pdxfinder.repositories;

import org.junit.Before;
import org.junit.Test;
import org.pdxfinder.BaseTest;
import org.pdxfinder.dao.ExternalDataSource;
import org.pdxfinder.dao.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Date;
import java.time.Instant;

/**
 * Tests for the Patient data repository
 */

public class PatientRepositoryTest extends BaseTest {

    private final static Logger log = LoggerFactory.getLogger(PatientRepositoryTest.class);
    private String extDsName = "TEST_SOURCE";

    @Autowired
    private ExternalDataSourceRepository externalDataSourceRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Before
    public void setupDb() {

        patientRepository.deleteAll();
        externalDataSourceRepository.deleteAll();

        ExternalDataSource ds = externalDataSourceRepository.findByName(extDsName);
        if (ds == null) {
            log.debug("External data source {} not found. Creating", extDsName);
            ds = new ExternalDataSource(extDsName, extDsName, extDsName, extDsName, Date.from(Instant.now()));
            externalDataSourceRepository.save(ds);
        }
    }

    @Test
    public void persistedPatientShouldBeRetrievableFromGraphDb() throws Exception {

        ExternalDataSource externalDataSource = externalDataSourceRepository.findByAbbreviation(extDsName);

        Patient femalePatient = new Patient("-9999", "F", null, null, externalDataSource);
        patientRepository.save(femalePatient);

        Patient foundFemalePatient = patientRepository.findBySex("F").iterator().next();
        assert (foundFemalePatient != null);
        assert (foundFemalePatient.getSex().equals("F"));

        log.info(foundFemalePatient.toString());

        patientRepository.delete(patientRepository.findByExternalId("-9999"));
        Patient notFoundFemalePatient = patientRepository.findByExternalId("-9999");
        assert (notFoundFemalePatient == null);


    }

}