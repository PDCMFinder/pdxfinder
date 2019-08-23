package org.pdxfinder.commands.dataloaders;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.pdxfinder.commands.BaseTest;

import static org.junit.Assert.*;

public class UniversalLoaderTest extends BaseTest {

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @InjectMocks
    private UniversalLoader loader;

    @Test
    public void Given_BaseFilePathContainsTrailingSlash_WhenLoading_PathIsCleaned() {
        assertEquals("file/path", loader.stripTrailingSlash("file/path/"));
    }

    @Test
    public void Given_BaseFilePathDoesNotContainTrailingSlash_WhenLoading_PathIsClean() {
        assertEquals("file/path", loader.stripTrailingSlash("file/path"));
    }

}