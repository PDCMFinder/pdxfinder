package org.pdxfinder.commands.dataloaders;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LoaderBaseTest {
    @Before
    public void setup() {}

    @Test
    public void correctAccessibilityPassesValidation() {
        Assert.assertEquals(
                "academia",
                LoaderBase.validateAccessibility("academia")
        );
        Assert.assertEquals(
                "industry or academia",
                LoaderBase.validateAccessibility("industry or academia")
        );
        Assert.assertEquals(
                "free text",
                LoaderBase.validateAccessibility("free text")
        );
    }

    @Test
    public void validateAccessibilityHandlesNull() {
        Assert.assertEquals(
                "",
                LoaderBase.validateAccessibility(null)
        );
    }

    @Test
    public void correctAccessModalityPassesValidation() {
        Assert.assertEquals(
            "transnational access",
             LoaderBase.validateModality("transnational access")
        );
        Assert.assertEquals(
            "TA",
            LoaderBase.validateModality("TA")
        );
    }

    @Test
    public void validateModalityHandlesNull() {
        Assert.assertEquals(
                "",
                LoaderBase.validateModality(null)
        );
    }

}
