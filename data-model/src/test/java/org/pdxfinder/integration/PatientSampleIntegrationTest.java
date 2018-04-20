package org.pdxfinder.integration;

import org.junit.Before;
import org.junit.Test;
import org.pdxfinder.BaseTest;
import org.pdxfinder.dao.*;
import org.pdxfinder.repositories.*;
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
    private String extDsNameAlternate = "ALTERNATE_TEST_SOURCE";
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
    private ExternalDataSourceRepository externalDataSourceRepository;

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
        externalDataSourceRepository.deleteAll();
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




        ExternalDataSource ds = externalDataSourceRepository.findByName(extDsName);
        if (ds == null) {
            log.debug("External data source {} not found. Creating", extDsName);
            ds = new ExternalDataSource(extDsName, extDsName, extDsName, extDsName, Date.from(Instant.now()));
            externalDataSourceRepository.save(ds);
        }

        ExternalDataSource dsAlt = externalDataSourceRepository.findByName(extDsNameAlternate);
        if (dsAlt == null) {
            log.debug("External data source {} not found. Creating", extDsNameAlternate);
            dsAlt = new ExternalDataSource(extDsNameAlternate, extDsNameAlternate, extDsNameAlternate, extDsNameAlternate, Date.from(Instant.now()));
            externalDataSourceRepository.save(dsAlt);
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
        ExternalDataSource externalDataSource = externalDataSourceRepository.findByAbbreviation(extDsName);
        MolecularCharacterization mc = mcRepository.findByTechnology(molCharTechnology);
        Marker marker = markerRepository.findBySymbol(markerSymbol);


        MarkerAssociation ma = new MarkerAssociation();
        ma.setMarker(marker);
        mc.setMarkerAssociations(Collections.singletonList(ma));

        for (Integer i = 0; i < 20; i++) {

            String sex = i % 2 == 0 ? "M" : "F";
            Long age = Math.round(Math.random() * 90);

            Sample sample = new Sample("sample-" + i, tumorType, "TEST_DIAGNOSIS", tissue, tissue, "Surgical Resection", "TEST_CLASSIFICATION", false ,externalDataSource.getAbbreviation());
            sampleRepository.save(sample);

            Patient patient = new Patient(Double.toString(Math.pow(i, age)), sex, null, null, externalDataSource);
            PatientSnapshot ps = new PatientSnapshot(patient, "67");
            patient.hasSnapshot(ps);
            Sample s = new Sample("test", tumorType, "adinocarcinoma", tissue, null, "Surgical Resection", "F", false,externalDataSource.getAbbreviation());
            s.normalTissue = Boolean.FALSE;
            s.setMolecularCharacterizations(new HashSet<>(Collections.singletonList(mc)));

            ps.setSamples(new HashSet<>(Collections.singletonList(s)));

            patientRepository.save(patient);

        }

        ExternalDataSource externalDataSourceAlternate = externalDataSourceRepository.findByAbbreviation(extDsNameAlternate);

        for (Integer i = 0; i < 22; i++) {

            String sex = i % 2 == 0 ? "M" : "F";
            Long age = Math.round(Math.random() * 80);

            Sample sample = new Sample("sample-" + i, tumorType, "TEST_DIAGNOSIS", tissue, tissue, "Surgical Resection", "TEST_CLASSIFICATION", false,externalDataSource.getAbbreviation());
            sampleRepository.save(sample);

            PatientSnapshot ps = new PatientSnapshot(null, "67");
            ps.setSamples(new HashSet<>(Arrays.asList(sample)));
            patientsnapshotRepository.save(ps);

        }

    }

}
