package org.pdxfinder.rdbms.repositories;

import org.pdxfinder.rdbms.dao.MappingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;


/**
 * Created by abayomi on 25/07/2019.
 */
@Repository
public interface MappingEntityRepository extends JpaRepository<MappingEntity, Long> {


    MappingEntity findByMappingKey(String mappingKey);

    List<MappingEntity> findByMappedTermLabel(String mappedTermLabel);


    @Query(value = "Select distinct me from MappingEntity me JOIN me.mappingValues mv " +
            "WHERE ((lower(me.entityType) = lower(:entityType)) OR :entityType = '') "+
            "AND ( (lower(KEY(mv)) = lower(:mappingLabel) AND lower(mv) = lower(:mappingValue)) OR  :mappingValue = '' ) "
    )
    Page<MappingEntity> findByMultipleFilters(@Param("entityType") String entityType,

                                              @Param("mappingLabel") String mappingLabel,
                                              @Param("mappingValue") String mappingValue,

                                              Pageable pageable);



    @Query(value = "select me from MappingEntity me JOIN me.mappingValues mv WHERE KEY(mv) = :dataKey AND mv = :dataValue ")
    List<MappingEntity> findByAttributeAndValue(@Param("dataKey") String dataKey,
                                                @Param("dataValue") String dataValue);


}