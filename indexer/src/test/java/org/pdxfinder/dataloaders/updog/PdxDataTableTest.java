package org.pdxfinder.dataloaders.updog;

import org.junit.Test;
import tech.tablesaw.api.Table;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PdxDataTableTest {

    PdxDataTable pdxDataTable = mock(PdxDataTable.class);
    Table table = mock(Table.class);
    File file = new File("does/not/exist.tsv");

    @Test
    public void readTsv() throws Exception {
        when(pdxDataTable.readTsv(file)).thenReturn(table);
        assertEquals(table, pdxDataTable.readTsv(file));
    }

}