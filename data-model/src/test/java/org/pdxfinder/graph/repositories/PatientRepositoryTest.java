package org.pdxfinder.graph.repositories;

import org.junit.Before;
import org.junit.Test;
import org.pdxfinder.BaseTest;
import org.pdxfinder.graph.dao.Group;
import org.pdxfinder.graph.dao.Patient;
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
    private String extDsAbbrev = "TS";

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Before
    public void setupDb() {

        patientRepository.deleteAll();
        groupRepository.deleteAll();

        Group ds = groupRepository.findByNameAndType(extDsName, "Provider");
        if(ds == null){
            log.info("Group not found. Creating", extDsName);

            ds = new Group(extDsName, extDsAbbrev, "Provider");
            groupRepository.save(ds);

        }
    }

    @Test
    public void persistedPatientShouldBeRetrievableFromGraphDb() throws Exception {

        Group group = groupRepository.findByNameAndType(extDsName, "Provider");

        Patient femalePatient = new Patient("-9999", "F", null, null, group);
        patientRepository.save(femalePatient);

        Patient foundFemalePatient = patientRepository.findBySex("F").iterator().next();
        assert (foundFemalePatient != null);
        assert (foundFemalePatient.getSex().equals("F"));

        log.info(foundFemalePatient.toString());

        patientRepository.delete(patientRepository.findByExternalIdAndGroup("-9999", group));
        Patient notFoundFemalePatient = patientRepository.findByExternalIdAndGroup("-9999", group);
        assert (notFoundFemalePatient == null);


    }

}