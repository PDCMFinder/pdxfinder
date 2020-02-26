package org.pdxfinder.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class DataProvidersTest {

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

}