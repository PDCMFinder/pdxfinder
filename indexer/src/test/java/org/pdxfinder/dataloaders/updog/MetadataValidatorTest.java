package org.pdxfinder.dataloaders.updog;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import tech.tablesaw.api.Table;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class MetadataValidatorTest {

    public Map<String, Table> completeFileSet = new HashMap<>();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        Arrays.asList(
            "metadata-loader.tsv",
            "metadata-checklist.tsv",
            "metadata-sharing.tsv",
            "metadata-model_validation.tsv",
            "metadata-patient.tsv",
            "metadata-model.tsv",
            "metadata-sample.tsv"
        ).stream().forEach(s -> completeFileSet.put(s, Table.create()));

    }

    @InjectMocks
    private MetadataValidator metadataValidator;

    @Test
    public void Given_EmptyMap_WhenValidating_FailsValidation() {
        Map<String, Table> emptyHashMap = new HashMap<>();
        assertEquals(false, metadataValidator.passesValidation(emptyHashMap));
    }

    @Test
    public void Given_IncompleteMap_WhenValidating_FailsValidation() {
        Map<String, Table> incompleteFileSet;
        incompleteFileSet = completeFileSet;
        incompleteFileSet.remove("metadata-patient.tsv");
        assertEquals(false, metadataValidator.passesValidation(incompleteFileSet));
    }

    @Test
    public void Given_CompleteMap_WhenValidating_PassesValidation() {
        assertEquals(true, metadataValidator.passesValidation(completeFileSet));
    }

    @Test
    public void Given_ExtraTableInMap_WhenValidating_PassesValidation() {
        Map<String, Table> completeFileSetPlusOne;
        completeFileSetPlusOne = completeFileSet;
        completeFileSetPlusOne.put("extra-file.tsv", Table.create());
        assertEquals(true, metadataValidator.passesValidation(completeFileSetPlusOne));
    }


}