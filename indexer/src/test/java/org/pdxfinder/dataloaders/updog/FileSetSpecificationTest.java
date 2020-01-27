package org.pdxfinder.dataloaders.updog;

import org.junit.Test;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.*;

public class FileSetSpecificationTest {

    @Test
    public void builderMethods_givenInstantiation_allReturnInstanceOfThisClass() {
        FileSetSpecification fileSetSpecification = FileSetSpecification.create();
        assertThat(fileSetSpecification, isA(FileSetSpecification.class));
    }

    @Test public void builderMethods_givenRequiredFileSetList_setsRequiredFiles() {
        FileSetSpecification fileSetSpecification = FileSetSpecification.create();
        assertEquals(
            requiredFileSet,
            fileSetSpecification
                .addRequiredFileList(requiredFileSet)
                .build().getRequiredFileList()
        );
    }

    private final Set<String> requiredFileSet = Stream
        .of("file_1.tsv", "file_2.tsv")
        .collect(Collectors.toSet());

}