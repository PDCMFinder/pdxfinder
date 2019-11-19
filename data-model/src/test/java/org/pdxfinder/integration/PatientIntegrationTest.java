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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PatientIntegrationTest extends BaseTest {

    @Autowired
    PatientRepository patientRepository;

    @Autowired
    PatientSnapshotRepository patientSnapshotRepository;

    @Autowired
    GroupRepository groupRepository;


    Patient patient;
    Patient patient2;
    Patient patient3;

    Group providerGroup;

    @Before
    public void setupDB(){

        patientRepository.deleteAll();
        patientSnapshotRepository.deleteAll();
        groupRepository.deleteAll();

        providerGroup = new Group("testgroup", "tg", "groupdescription", "academia", "", "");

        patient = new Patient("p1", "male", "-", "-", providerGroup);

        PatientSnapshot ps1 = new PatientSnapshot();
        ps1.setAgeAtCollection("40");

        PatientSnapshot ps2 = new PatientSnapshot();
        ps2.setAgeAtCollection("45");

        patient.addSnapshot(ps1);
        patient.addSnapshot(ps2);


        patient2 = new Patient();
        patient2.setExternalId("p2");
        patient2.setSex("female");
        patient2.setRace("-");
        patient2.setEthnicity("-");

        PatientSnapshot ps3 = new PatientSnapshot();
        ps3.setAgeAtCollection("76");
        ps3.setDateAtCollection("2000-01-01");
        ps3.setCollectionEvent("event1");
        ps3.setElapsedTime("0");
        Set<PatientSnapshot> psSet = new HashSet<>();
        psSet.add(ps3);
        patient2.setSnapshots(psSet);
        List<Group> groups = new ArrayList<>();
        groups.add(providerGroup);
        patient2.setGroups(groups);
        patient2.setFirstDiagnosis("firstdiag");
        patient2.setAgeAtFirstDiagnosis("68");
        patient2.setDataSource(providerGroup.getAbbreviation());
        patient2.setCancerRelevantHistory("-");

        patient3 = new Patient();

    }

    @Test
    public void Given_PatientWithProvider_When_PatientAndSnapshotPresent_Then_PatientIdAndAgeIsCorrect(){

        Assert.assertEquals("p1", patient.getExternalId());
        Assert.assertEquals(providerGroup.getAbbreviation(), patient.getProviderGroup().getAbbreviation());
        Assert.assertEquals("45", patient.getLastSnapshot().getAgeAtCollection() );
        Assert.assertEquals(null, patient3.getProviderGroup());

    }

    @Test
    public void Given_Patient_When_PatientDetailsChecked_Then_PatientFirstDiagAndAgeIsCorrect(){

        Assert.assertEquals("firstdiag", patient2.getFirstDiagnosis());
        Assert.assertEquals("68", patient2.getAgeAtFirstDiagnosis() );
        Assert.assertEquals("-", patient2.getCancerRelevantHistory());

    }


    @Test
    public void Given_PatientWithSnapshot_When_PatientSnapshotGetByCollection_Then_CorrectSnapshotIsReturned(){


        Assert.assertEquals("76", patient2.getSnapShotByCollection("76", "2000-01-01", "event1", "0").getAgeAtCollection() );
        Assert.assertEquals("2000-01-01", patient2.getSnapshotByDate("2000-01-01").getDateAtCollection());

        Assert.assertEquals(null, patient3.getSnapShotByCollection("76", "2000-01-01", "event1", "0"));
        Assert.assertEquals(null, patient3.getSnapshotByDate("2000-01-01"));
    }

}
