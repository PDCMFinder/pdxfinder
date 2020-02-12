package org.pdxfinder.dataloaders.updog;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.pdxfinder.dataloaders.updog.Updog.concatenate;

public class UpdogTest {

    @InjectMocks private Updog updogUnderTest;
    @Mock private UtilityService utilityService;
    @Mock private DataImportService dataImportService;
    @Mock private Reader reader;
    @Mock private Validator validator;

    @Test public void concatenate_whenGivenTwoEmptyLists_returnsEmptyList() {
        List<String> emptyList = Collections.emptyList();
        List<String> expected = emptyList;
        assertEquals(
            expected,
            concatenate(emptyList, emptyList)
        );
    }

    @Test public void concatenate_whenGivenThreeLists_returnsConcatenatedList() {
        List<String> listOne = Arrays.asList("item 1");
        List<String> listTwo = Arrays.asList("item 2");
        List<String> listThree = Arrays.asList("item 3");
        List<String> expected = Arrays.asList("item 1", "item 2", "item 3");
        assertEquals(
            expected,
            concatenate(listOne, listTwo, listThree)
        );
    }

}