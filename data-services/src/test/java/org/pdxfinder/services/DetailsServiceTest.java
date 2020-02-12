package org.pdxfinder.services;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.pdxfinder.BaseTest;
import org.pdxfinder.graph.dao.*;
import org.pdxfinder.graph.repositories.*;
import org.pdxfinder.services.dto.PatientDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class DetailsServiceTest extends BaseTest {

    private Logger log = LoggerFactory.getLogger(DetailsServiceTest.class);

    @Mock
    private PatientRepository patientRepository;
    @Mock
    private ModelCreationRepository modelCreationRepository;
    @Mock
    private SampleRepository sampleRepository;
    @Mock
    private MolecularCharacterizationRepository molecularCharacterizationRepository;
    @Mock
    private SpecimenRepository specimenRepository;
    @Mock
    private PatientService patientService;
    @Mock
    private PublicationService publicationService;

    @InjectMocks
    private DetailsService detailsService;

    private String dataSource;
    private String modelId;

    private static final String EXTERNAL_ID = "";
    private static final String SEX = "Female";
    private static final String RACE = "White";
    private static final String ETHNICITY = "Hispanic";
    private static final String GROUP_NAME = "xxxx";
    private static final String ABBREV = "xxxx";
    private static final String GROUP_TYPE = "Provider";

    private static final Group GROUP = new Group(GROUP_NAME, ABBREV, GROUP_TYPE);
    private static final Patient PATIENT = new Patient(EXTERNAL_ID, SEX, RACE, ETHNICITY, GROUP);


    @Test
    public void given_DataSourceAndModelId_When_GetModelDetailsInvoked_Then_ReturnDetailsDTO() {

        // given
        this.dataSource = "JAX";
        this.modelId = "TM0025";

        String ageAtCollection = "90";
        PatientSnapshot snapshot = new PatientSnapshot(PATIENT, ageAtCollection);
        PATIENT.setSnapshots(new HashSet<>(Collections.singletonList(snapshot)));

        Sample sample = new Sample("", new TumorType(), "adenocarcinoma",
                                   new Tissue(),
                                   new Tissue(),
                                   "",
                                   "",
                                   true,
                                   this.dataSource);
        sample.setSampleToOntologyRelationShip(new SampleToOntologyRelationship("","", new Sample(), new OntologyTerm()));


        when(this.patientRepository.findByDataSourceAndModelId(anyString(), anyString()))
                .thenReturn(PATIENT);

        when(this.modelCreationRepository.findByDataSourceAndSourcePdxId(this.dataSource, this.modelId))
                .thenReturn(new ModelCreation());

        when(this.sampleRepository.findPatientSampleWithDetailsByDataSourceAndPdxId(this.dataSource, this.modelId))
                .thenReturn(sample);

        when(this.patientService.getPatientDetails(this.dataSource, this.modelId))
                .thenReturn(new PatientDTO());


        // When
       // detailsService.getModelDetails(this.dataSource, this.modelId);

        // Then


    }


}
