package org.pdxfinder.services;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.pdxfinder.BaseTest;
import org.pdxfinder.graph.repositories.MolecularCharacterizationRepository;
import org.pdxfinder.rdbms.dao.MappingEntity;
import org.pdxfinder.rdbms.repositories.MappingEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import static org.mockito.Mockito.when;

import java.util.Optional;

/*
 * Created by abayomi on 27/08/2019.
 */
public class MappingServiceTest extends BaseTest {

    @Autowired
    private MappingService mappingService;

    @MockBean
    private MappingEntityRepository mappingEntityRepository;

    @Test
    @Ignore
    public void testGetMappingEntityById(){

        MappingEntity mappingEntity = new MappingEntity();
        mappingEntity.setEntityId(1L);
        mappingEntity.setMappedTermLabel("Melanoma");
        //Set Mapping Key

        Long id = mappingEntity.getEntityId();
        Optional<MappingEntity> mockEntity = Optional.of(mappingEntity);

        when(mappingEntityRepository.findByEntityId(id)).thenReturn(mockEntity);
        assertThat(mappingService.getMappingEntityById(1)).isEqualTo(mappingEntity);
       // assertEquals(1, mappingService.getMappingEntityById(1).getMappingKey());

    }

}
