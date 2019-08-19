package org.pdxfinder.commands.dataloaders;

import org.junit.Test;
import org.mockito.Mock;


import org.pdxfinder.commands.BaseTest;
import org.pdxfinder.graph.dao.ModelCreation;
import org.pdxfinder.services.dto.LoaderDTO;

import java.util.List;


public class LoadIRCCTest extends BaseTest {

//    @Before
//    public void setUp() throws Exception {
//        MockitoAnnotations.initMocks(this.getClass());
//    }

//    @Before
//    public void init(){
//        MockitoAnnotations.initMocks(LoadIRCC.class);
//    }

    @Mock
    private List list;

    @Mock
    private LoaderDTO dto;

    @Mock
    private ModelCreation modelCreation;

//    @Autowired
//    void LoadIIRCTest(LoadIRCC loader) {
//        this.loader = loader;
//    }

//    @Spy
//    @InjectMocks
    private LoadIRCC loader;

    @Test
    public void addAccessModality() {
        dto.setModelCreation(modelCreation);
        loader.addAccessModality();
    }
}