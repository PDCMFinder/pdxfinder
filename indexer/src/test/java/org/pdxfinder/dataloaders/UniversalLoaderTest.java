package org.pdxfinder.dataloaders;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.pdxfinder.BaseTest;

import static org.junit.Assert.*;

public class UniversalLoaderTest extends BaseTest {

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void Given_BaseFilePathContainsTrailingSlash_WhenLoading_PathIsCleaned() {
        Assert.assertEquals("file/path", UniversalLoader.stripTrailingSlash("file/path/"));
    }

    @Test
    public void Given_BaseFilePathDoesNotContainTrailingSlash_WhenLoading_PathIsClean() {
        assertEquals("file/path", UniversalLoader.stripTrailingSlash("file/path"));
    }

}