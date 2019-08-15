package org.pdxfinder.commands.dataloaders;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.pdxfinder.graph.dao.ModelCreation;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.dto.LoaderDTO;


public class LoadIRCCTest {

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this.getClass());
    }

    @Mock
    LoadIRCC loader;

    @InjectMocks
    LoaderDTO dto;

    @InjectMocks
    ModelCreation modelCreation;

    @InjectMocks
    DataImportService dataImportService;

    @Test
    public void addAccessModality() {
        dto.setModelCreation(modelCreation);
        loader.addAccessModality();
    }
}