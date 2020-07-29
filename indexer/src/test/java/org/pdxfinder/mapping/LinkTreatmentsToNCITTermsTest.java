package org.pdxfinder.mapping;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pdxfinder.BaseTest;
import org.pdxfinder.graph.dao.OntologyTerm;
import org.pdxfinder.graph.dao.Treatment;
import org.pdxfinder.rdbms.dao.MappingEntity;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.MappingService;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class LinkTreatmentsToNCITTermsTest extends BaseTest {

    @MockBean
    private MappingService mappingService;
    @MockBean
    private DataImportService dataImportService;

    LinkTreatmentsToNCITTerms linkTreatmentsToNCITTerms;


    @Before
    public void init(){
        linkTreatmentsToNCITTerms = new LinkTreatmentsToNCITTerms(mappingService, dataImportService);
    }


    @Test
    public void Given_Treatments_When_AddRelationshipToTreatments_Then_NewRelationshipIsPresent(){
        when(mappingService.getTreatmentMapping(any(), any())).thenReturn(getMappingEntity());
        when(dataImportService.findOntologyTermByUrl(any())).thenReturn(getOntologyTerm());

        List<Treatment> treatments = getTreatments();
        linkTreatmentsToNCITTerms.addRelationshipToTreatments(treatments, "DS");
        Assert.assertEquals("otlabel1",treatments.get(0).getTreatmentToOntologyRelationship().getOntologyTerm().getLabel());

    }



    private MappingEntity getMappingEntity(){

        MappingEntity me = new MappingEntity();
        me.setJustification("");
        me.setMapType("direct");
        me.setMappedTermLabel("label1");

        return me;
    }

    private OntologyTerm getOntologyTerm(){
        OntologyTerm ot = new OntologyTerm();
        ot.setLabel("otlabel1");
        ot.setUrl("url1");
        return ot;
    }

    private List<Treatment> getTreatments(){

        List<Treatment> treatments = new ArrayList<>();

        Treatment t1 = new Treatment("drug1");
        treatments.add(t1);

        return treatments;
    }
}
