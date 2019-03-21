package org.pdxfinder.rdbms.repositories;

import org.pdxfinder.rdbms.dao.MappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Map;

public interface MappingEntityRepository extends JpaRepository<MappingEntity, Long> {


    MappingEntity findByMappingValues(Map<String, String> mappingValues);


    MappingEntity findByMappingKey(String mappingKey);


    List<MappingEntity> findByMappedTermLabel(String mappedTermLabel);



}
