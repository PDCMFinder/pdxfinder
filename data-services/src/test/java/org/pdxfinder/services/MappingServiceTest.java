package org.pdxfinder.services;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.pdxfinder.BaseTest;
import org.pdxfinder.rdbms.repositories.MappingEntityRepository;
import static org.mockito.Mockito.*;

public class MappingServiceTest extends BaseTest {

    @Mock
    private MappingEntityRepository mappingEntityRepository;

    @InjectMocks
    private MappingService mappingService;

    @Before
    public void setup() {
        doNothing().when(this.mappingEntityRepository).deleteAll();
    }

    @Test
    public void given_When_PurgeMappingDatabaseInvoked_Then_MappingsDeleted() {

        // When
        mappingService.purgeMappingDatabase();

        // Then
        verify(mappingEntityRepository, atLeast(1)).deleteAll();

    }



}
