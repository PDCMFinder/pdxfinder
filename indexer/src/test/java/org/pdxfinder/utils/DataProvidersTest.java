package org.pdxfinder.utils;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
//import org.pdxfinder.dataloaders.LoadUniversal;
import org.springframework.context.ApplicationContext;

import static org.junit.Assert.*;
//import static org.mockito.Matchers.anyString;
//import static org.mockito.Mockito.doNothing;
//import static org.mockito.Mockito.when;

public class DataProvidersTest {

//    @Mock LoadUniversal loadUniversal;
    @Mock ApplicationContext applicationContext;
    @InjectMocks DataProviders dataProviders;

    @Test
    public void getProvidersFrom_givenGroupAll_returnAllProviders() {
        DataProviders.DataProvider[] expected = DataProviders.DataProvider.values();
        DataProviders.DataProviderGroup all = DataProviders.DataProviderGroup.All;
        DataProviders.DataProvider[] providersFromAll = DataProviders.getProvidersFrom(all).toArray(new DataProviders.DataProvider[0]);
        assertEquals(
            expected,
            providersFromAll
        );
    }

//    @Test
//    public void load_givenUniversalLoaderRequired_called() throws Exception {
//        doNothing().when(loadUniversal).run(anyString());
//    }
}