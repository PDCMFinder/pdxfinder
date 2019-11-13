package org.pdxfinder.integration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pdxfinder.BaseTest;
import org.pdxfinder.graph.dao.Group;
import org.pdxfinder.graph.dao.Patient;
import org.pdxfinder.graph.dao.PatientSnapshot;
import org.pdxfinder.graph.repositories.GroupRepository;
import org.pdxfinder.graph.repositories.PatientRepository;
import org.pdxfinder.graph.repositories.PatientSnapshotRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class PatientIntegrationTest extends BaseTest {

    @Autowired
    PatientRepository patientRepository;

    @Autowired
    PatientSnapshotRepository patientSnapshotRepository;

    @Autowired
    GroupRepository groupRepository;


    @Before
    public void setupDB(){

        patientRepository.deleteAll();
        patientSnapshotRepository.deleteAll();
        groupRepository.deleteAll();

        Group providerGroup = new Group("testgroup", "tg", "groupdescription", "academia", "", "");

        Patient p = new Patient("p1", "male", "-", "-", providerGroup);

        PatientSnapshot ps1 = new PatientSnapshot();
        ps1.setAgeAtCollection("40");

        PatientSnapshot ps2 = new PatientSnapshot();
        ps2.setAgeAtCollection("45");

        p.addSnapshot(ps1);
        p.addSnapshot(ps2);

        patientRepository.save(p);

    }

    @Test
    public void PatientTest(){


        Group g = groupRepository.findByAbbrevAndType("tg", "Provider");
        Patient p = patientRepository.findByExternalIdAndGroup("p1", g);

        Assert.assertEquals(p.getExternalId(), "p1");
        Assert.assertEquals(p.getProviderGroup().getAbbreviation(), g.getAbbreviation());
        Assert.assertEquals(p.getLastSnapshot().getAgeAtCollection(), "45");

    }



}
