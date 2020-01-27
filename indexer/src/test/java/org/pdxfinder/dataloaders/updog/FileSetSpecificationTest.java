package org.pdxfinder.dataloaders.updog;

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.*;

public class FileSetSpecificationTest {

    @Test
    public void builderMethods_givenInstantiation_allReturnInstanceOfThisClass() {
        FileSetSpecification fileSetSpecification = FileSetSpecification.create();
        assertThat(fileSetSpecification, isA(FileSetSpecification.class));
    }

    @Test public void builderMethods_givenRequiredFileSetList_setsRequiredFiles() {
        String expected = Arrays.asList("file_1.tsv", "file_2.tsv").toString();
        FileSetSpecification fileSetSpecification = FileSetSpecification.create();
        assertEquals(
            expected,
            fileSetSpecification
                .addRequiredFileList(Arrays.asList("file_1.tsv", "file_2.tsv"))
                .build().getRequiredFileList().toString()
        );
    }

}