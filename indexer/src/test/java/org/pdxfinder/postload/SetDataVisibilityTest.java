package org.pdxfinder.postload;

import org.junit.Assert;
import org.junit.Test;
import org.pdxfinder.graph.dao.MolecularCharacterization;
import org.pdxfinder.services.DataImportService;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;

public class SetDataVisibilityTest {

    @MockBean
    DataImportService dataImportService;


    @Test
    public void Given_MolecularCharacterizations_When_DisablingVisibility_Then_VisibilityDisabled(){

        SetDataVisibility setDataVisibility = new SetDataVisibility(dataImportService);
        List<MolecularCharacterization> molchars = getMolchars();
        setDataVisibility.disableVisibility(molchars);
        Assert.assertEquals(false, molchars.get(0).isVisible());


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
