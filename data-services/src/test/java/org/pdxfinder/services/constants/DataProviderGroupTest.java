package org.pdxfinder.services.constants;

import org.junit.Test;

import static org.junit.Assert.*;

public class DataProviderGroupTest {

    @Test
    public void getProvidersFrom_givenGroupAll_returnAllProviders() {
        DataProvider[] expected = DataProvider.values();
        DataProviderGroup all = DataProviderGroup.All;
        DataProvider[] providersFromAll = DataProviderGroup.getProvidersFrom(all).toArray(new DataProvider[0]);

        assertEquals(
            expected,
            providersFromAll
        );
    }

}