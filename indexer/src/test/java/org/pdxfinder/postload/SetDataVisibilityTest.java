package org.pdxfinder.postload;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pdxfinder.BaseTest;
import org.pdxfinder.graph.dao.MolecularCharacterization;
import org.pdxfinder.services.DataImportService;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class SetDataVisibilityTest extends BaseTest {

    @MockBean
    DataImportService dataImportService;

    SetDataVisibility setDataVisibility;

    @Before
    public void init(){

        setDataVisibility = new SetDataVisibility(dataImportService);
    }

    @Test
    public void Given_MolecularCharacterizations_When_DisablingVisibility_Then_VisibilityDisabled(){

        List<MolecularCharacterization> molchars = getMolchars();
        setDataVisibility.disableVisibility(molchars);
        Assert.assertEquals(false, molchars.get(0).isVisible());
    }

    @Test
    public void Given_DataSource_When_ApplyRules_Then_RulesApplied(){

        when(dataImportService.findMolcharNumberByDataSource("TEST")).thenReturn(2);
        when(dataImportService.findMolcharByDataSourceSkipLimit("TEST", 0, 50)).thenReturn(getMolchars());

        setDataVisibility.applyDataVisibilityRules("TEST");
        List<MolecularCharacterization> molcharList = getMolchars();
        verify(dataImportService, times(1)).saveMolecularCharacterizations(any());
    }


    private List<MolecularCharacterization> getMolchars(){

        MolecularCharacterization mc1 = new MolecularCharacterization();
        MolecularCharacterization mc2 = new MolecularCharacterization();
        List<MolecularCharacterization> list = new ArrayList<>();

        list.add(mc1);
        list.add(mc2);
        return list;
    }

}
