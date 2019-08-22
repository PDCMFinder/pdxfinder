package org.pdxfinder.commands.dataloaders;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.pdxfinder.commands.BaseTest;
import org.pdxfinder.graph.dao.Group;
import org.pdxfinder.graph.dao.ModelCreation;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class LoadIRCCTest extends BaseTest {

    private Group g;
    private Group badG;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        g = new Group();
        g.setName("transnational access");

        badG = new Group();
        badG.setName("another group");

    }

    @MockBean
    UtilityService utilityService;

    @MockBean
    DataImportService dataImportService;

    @InjectMocks
    private LoadIRCC loader;

    @Test
    public void addAccessModality() {

        when(dataImportService.getAccessibilityGroup("", "transnational access")).thenReturn(g);

        ModelCreation mc = new ModelCreation();
        loader.dto.setModelCreation(mc);
        loader.addAccessModality();

        assertThat(loader.dto.getModelCreation().getGroups().contains(g), is(true));
        assertThat(loader.dto.getModelCreation().getGroups().contains(badG), is(false));
    }

}