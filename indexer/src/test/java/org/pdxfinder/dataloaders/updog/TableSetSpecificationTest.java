package org.pdxfinder.dataloaders.updog;

import org.junit.Test;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.*;

public class TableSetSpecificationTest {

    @Test
    public void builderMethods_givenInstantiation_allReturnInstanceOfThisClass() {
        TableSetSpecification tableSetSpecification = TableSetSpecification.create();
        assertThat(tableSetSpecification, isA(TableSetSpecification.class));
    }

    @Test public void builderMethods_givenRequiredFileSetList_setsRequiredFiles() {
        TableSetSpecification tableSetSpecification = TableSetSpecification.create();
        assertEquals(
            requiredFileSet,
            tableSetSpecification
                .addRequiredFileList(requiredFileSet)
                .getRequiredFileList()
        );
    }

    private final Set<String> requiredFileSet = Stream
        .of("file_1.tsv", "file_2.tsv")
        .collect(Collectors.toSet());

}