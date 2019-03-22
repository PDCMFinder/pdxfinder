package org.pdxfinder.rdbms.repositories;

import org.pdxfinder.rdbms.dao.MappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface MappingEntityRepository extends JpaRepository<MappingEntity, Long> {

    MappingEntity findByMappingValues(Map<String, String> mappingValues);


    MappingEntity findByMappingKey(String mappingKey);


    List<MappingEntity> findByMappedTermLabel(String mappedTermLabel);
}