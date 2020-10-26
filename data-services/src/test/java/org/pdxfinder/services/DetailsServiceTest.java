package org.pdxfinder.services;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pdxfinder.BaseTest;
import org.pdxfinder.graph.dao.MarkerAssociation;
import org.pdxfinder.graph.dao.MolecularCharacterization;
import org.pdxfinder.graph.dao.MolecularData;
import org.pdxfinder.graph.dao.Sample;
import org.pdxfinder.graph.repositories.MolecularCharacterizationRepository;
import org.pdxfinder.graph.repositories.SampleRepository;
import org.pdxfinder.services.dto.MolecularDataTableDTO;
import static org.mockito.Mockito.*;
import java.util.HashMap;
import java.util.Map;

public class DetailsServiceTest extends BaseTest {

    @InjectMocks
    private DetailsService detailsService;

    @Mock
    MolecularCharacterizationRepository molecularCharacterizationRepository;

    @Mock
    SampleRepository sampleRepository;

    @Mock
    ReferenceDbService referenceDbService;

    Map<String, MolecularCharacterization> molchars;
    Sample sample;

    @Before
    public void init(){
        MockitoAnnotations.initMocks(this);
        molchars = initMolchars();
        sample = new Sample();
        sample.setSourceSampleId("test");
    }

    @Test
    public void given_MolcharId_When_GetMolcharTable_Then_CorrectContentReturned(){
        when(detailsService.getMolcharData("1")).thenReturn(molchars.get("1"));
        when(detailsService.getMolcharData("2")).thenReturn(molchars.get("2"));
        when(detailsService.getMolcharData("3")).thenReturn(molchars.get("3"));
        when(sampleRepository.findSampleByMolcharId(anyLong())).thenReturn(sample);
        when(referenceDbService.getReferenceData(any(), any())).thenReturn(new HashMap<>());
        MolecularDataTableDTO table1 = detailsService.getMolecularDataTable("1", false);
        MolecularDataTableDTO table2 = detailsService.getMolecularDataTable("2", false);
        MolecularDataTableDTO table3 = detailsService.getMolecularDataTable("3", false);

        Assert.assertEquals(3, table1.getMolecularDataRows().size());
        Assert.assertEquals(0, table2.getMolecularDataRows().size());
        Assert.assertEquals(0, table3.getMolecularDataRows().size());

    }




    private Map<String, MolecularCharacterization> initMolchars(){

        Map<String, MolecularCharacterization> molecularCharacterizationMap = new HashMap();

        MolecularCharacterization mc1 = new MolecularCharacterization();
        MarkerAssociation ma1 = new MarkerAssociation();
        ma1.setMolecularDataString("[{\"cytogeneticsResult\":\"postive\",\"marker\":\"ESR1\"},{\"cytogeneticsResult\":\"postive\",\"marker\":\"PGR\"},{\"cytogeneticsResult\":\"negative\",\"marker\":\"ERBB2\"}]");
        ma1.setDataPoints(3);
        mc1.addMarkerAssociation(ma1);
        mc1.setVisible(true);

        MolecularCharacterization mc2 = new MolecularCharacterization();
        MarkerAssociation ma2 = new MarkerAssociation();
        ma2.setMolecularDataString("[{\"cytogeneticsResult\":\"postive\",\"marker\":\"ESR1\"},{\"cytogeneticsResult\":\"postive\",\"marker\":\"PGR\"},{\"cytogeneticsResult\":\"negative\",\"marker\":\"ERBB2\"}]");
        ma2.setDataPoints(3);
        mc2.addMarkerAssociation(ma2);
        mc2.setVisible(false);

        MolecularCharacterization mc3 = new MolecularCharacterization();
        MarkerAssociation ma3 = new MarkerAssociation();
        ma3.setDataPoints(400);
        mc3.setVisible(true);

        for(int i=0;i<400;i++){
            MolecularData md = new MolecularData();
            md.setMarker("ABC");
            md.setCytogeneticsResult("negative");
            ma3.addMolecularData(md);
        }
        ma3.encodeMolecularData();
        mc3.addMarkerAssociation(ma3);


        molecularCharacterizationMap.put("1", mc1);
        molecularCharacterizationMap.put("2", mc2);
        molecularCharacterizationMap.put("3", mc3);

        return molecularCharacterizationMap;
    }

}
