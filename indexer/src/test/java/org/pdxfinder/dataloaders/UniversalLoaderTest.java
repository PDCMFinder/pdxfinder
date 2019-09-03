package org.pdxfinder.dataloaders;

import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.pdxfinder.BaseTest;

import java.util.Optional;

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
        Assert.assertEquals("file/path", UniversalLoader.stripTrailingSlash("file/path/"));
    }

    @Test
    public void Given_BaseFilePathDoesNotContainTrailingSlash_WhenLoading_PathIsClean() {
        assertEquals("file/path", UniversalLoader.stripTrailingSlash("file/path"));
    }

    @Test
    public void Given_NonExistingExcelTemplate_WhenLoading_ReturnOptionalEmpty() {
        Optional<Workbook> workbook = loader.getWorkbook(".", "does/not/exist/test.xlsx");
        assertEquals(Optional.empty(), workbook);
    }

}