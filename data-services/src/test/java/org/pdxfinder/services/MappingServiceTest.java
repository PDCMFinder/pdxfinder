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

    @Mock
    private UtilityService utilityService;

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

    @Test(expected = IllegalArgumentException.class)
    public void given_directoryAttack_when_entityIsWrittenToFile_Then_throwArgumentException()  {
        mappingService.setRootDir("/tmp");
        String attackVector = "../../..";
        mappingService.writeMappingsToFile(attackVector);
    }

    @Test
    public void given_AplhaNumericAndHyphenatedDir_when_entityIsWrittenToFile_Then_doNotThrowArgumentException() {
        mappingService.setRootDir("/tmp");
        String normalEntity= "TReatment-9090-HELLO--";
        mappingService.writeMappingsToFile(normalEntity);
    }




}
