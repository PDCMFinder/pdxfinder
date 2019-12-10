package org.pdxfinder.integration;

import org.junit.Before;
import org.junit.Test;
import org.pdxfinder.BaseTest;
import org.pdxfinder.graph.dao.*;
import org.pdxfinder.graph.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Date;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

/**
 * Test the integration of tumor to patient
 */
public class PatientSampleIntegrationTest extends BaseTest {

    private final static Logger log = LoggerFactory.getLogger(PatientSampleIntegrationTest.class);
    private String tumorTypeName = "TEST_TUMORTYPE";
    private String extDsName = "TEST_SOURCE";
    private String extDsAbbrev = "TS";

    private String extDsNameAlternate = "ALTERNATE_TEST_SOURCE";
    private String extDsNameAlternateAbbrev = "ATS";
    private String tissueName = "TEST_TISSUE";
    private String markerSymbol = "TEST_MARKER";
    private String molCharTechnology = "TEST_MOLCHAR";

    @Autowired
    private SampleRepository sampleRepository;

    @Autowired
    private TissueRepository tissueRepository;

    @Autowired
    private TumorTypeRepository tumorTypeRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PatientSnapshotRepository patientsnapshotRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private MolecularCharacterizationRepository mcRepository;

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private MarkerAssociationRepository markerAssociationRepository;

    @Before
    public void setupDb() {

        sampleRepository.deleteAll();
        patientRepository.deleteAll();
        groupRepository.deleteAll();
        tissueRepository.deleteAll();
        mcRepository.deleteAll();
        markerRepository.deleteAll();
        markerAssociationRepository.deleteAll();

        MolecularCharacterization mc = mcRepository.findByTechnology(molCharTechnology);
        if (mc == null) {
            log.debug("Molecular characterization {} not found. Creating", molCharTechnology);
            mc = new MolecularCharacterization(molCharTechnology);
            mcRepository.save(mc);
        }

        Marker marker = markerRepository.findBySymbol(markerSymbol);
        if (marker == null) {
            log.debug("Marker {} not found. Creating", markerSymbol);
            marker = new Marker(markerSymbol, markerSymbol);
            markerRepository.save(marker);
        }




        Group ds = groupRepository.findByNameAndType(extDsName, "Provider");
        if(ds == null){
            log.info("Group not found. Creating", extDsName);

            ds = new Group(extDsName, extDsAbbrev, "Provider");
            groupRepository.save(ds);

        }


        Group dsAlt = groupRepository.findByNameAndType(extDsNameAlternate, "Provider");
        if (dsAlt == null) {
            log.debug("Group {} not found. Creating", extDsNameAlternate);
            dsAlt = new Group(extDsNameAlternate, extDsNameAlternateAbbrev, "Provider");
            groupRepository.save(dsAlt);
        }

        TumorType tumorType = tumorTypeRepository.findByName(tumorTypeName);
        if (tumorType == null) {
            log.debug("Sample type {} not found. Creating", tumorTypeName);
            tumorType = new TumorType(tumorTypeName);
            tumorTypeRepository.save(tumorType);
        }

        Tissue tissue = tissueRepository.findByName(tissueName);
        if (tissue == null) {
            log.debug("Tissue {} not found. Creating", extDsName);
            tissue = new Tissue(tissueName);
            tissueRepository.save(tissue);
        }
    }

    @Test
    public void persistPatientAndTumor() throws Exception {

        TumorType tumorType = tumorTypeRepository.findByName(tumorTypeName);
        Tissue tissue = tissueRepository.findByName(tissueName);
        Group group = groupRepository.findByNameAndType(extDsName, "Provider");
        MolecularCharacterization mc = mcRepository.findByTechnology(molCharTechnology);
        Marker marker = markerRepository.findBySymbol(markerSymbol);


        MarkerAssociation ma = new MarkerAssociation();
        ma.setMarker(marker);
        mc.setMarkerAssociations(Collections.singletonList(ma));

        for (Integer i = 0; i < 20; i++) {

            String sex = i % 2 == 0 ? "M" : "F";
            Long age = Math.round(Math.random() * 90);

            Sample sample = new Sample("sample-" + i, tumorType, "TEST_DIAGNOSIS", tissue, tissue, "Surgical Resection", "TEST_CLASSIFICATION", false ,group.getAbbreviation());
            sampleRepository.save(sample);

            Patient patient = new Patient(Double.toString(Math.pow(i, age)), sex, null, null,null, group);
            PatientSnapshot ps = new PatientSnapshot(patient, "67");
            patient.hasSnapshot(ps);
            Sample s = new Sample("test", tumorType, "adinocarcinoma", tissue, null, "Surgical Resection", "F", false,group.getAbbreviation());
            s.normalTissue = Boolean.FALSE;
            s.setMolecularCharacterizations(new HashSet<>(Collections.singletonList(mc)));

            ps.setSamples(new HashSet<>(Collections.singletonList(s)));

            patientRepository.save(patient);

        }

        Group groupAlternate = groupRepository.findByNameAndType(extDsNameAlternate, "Provider");

        for (Integer i = 0; i < 22; i++) {

            String sex = i % 2 == 0 ? "M" : "F";
            Long age = Math.round(Math.random() * 80);

            Sample sample = new Sample("sample-" + i, tumorType, "TEST_DIAGNOSIS", tissue, tissue, "Surgical Resection", "TEST_CLASSIFICATION", false,group.getAbbreviation());
            sampleRepository.save(sample);

            PatientSnapshot ps = new PatientSnapshot(null, "67");
            ps.setSamples(new HashSet<>(Arrays.asList(sample)));
            patientsnapshotRepository.save(ps);

        }

    }

}
