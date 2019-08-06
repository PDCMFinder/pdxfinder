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
import java.util.Optional;


/**
 * Created by abayomi on 25/07/2019.
 */
@Repository
public interface MappingEntityRepository extends JpaRepository<MappingEntity, Long> {


    MappingEntity findByMappingKey(String mappingKey);

    Optional<MappingEntity> findByEntityId(Long entityId);

    List<MappingEntity> findByMappedTermLabel(String mappedTermLabel);


    @Query(value = "Select distinct me from MappingEntity me JOIN me.mappingValues mv " +
            "WHERE ((lower(me.entityType) = lower(:entityType)) OR :entityType = '') "+
            "AND ( (lower(KEY(mv)) = lower(:mappingLabel) AND lower(mv) = lower(:mappingValue)) OR  :mappingValue = '' ) "+
            "AND ((lower(me.mappedTermLabel) = lower(:mappedTermLabel)) OR :mappedTermLabel = '') "+

            "AND ((lower(me.mapType) = lower(:mapType)) OR :mapType = '') "+

            "AND ( :mappedTermsOnly = '' OR (me.mapType is not null )) "
    )
    Page<MappingEntity> findByMultipleFilters(@Param("entityType") String entityType,
                                              @Param("mappingLabel") String mappingLabel,
                                              @Param("mappingValue") String mappingValue,
                                              @Param("mappedTermLabel") String mappedTermLabel,
                                              @Param("mapType") String mapType,
                                              @Param("mappedTermsOnly") String mappedTermsOnly,

                                              Pageable pageable);


    @Query(value = "select me from MappingEntity me JOIN me.mappingValues mv WHERE KEY(mv) = :dataKey AND mv = :dataValue ")
    List<MappingEntity> findByAttributeAndValue(@Param("dataKey") String dataKey,
                                                @Param("dataValue") String dataValue);


    List<MappingEntity> findByEntityTypeAndMapTypeIsNotNull(String entityType);


    /*
    Query: SELECT DISTINCT lower(MAPPING_VALUES),
                    COUNT(CASE WHEN MAP_TYPE IS NULL THEN 1 END) AS UNMAPPED,
                    COUNT(CASE WHEN MAP_TYPE IS NOT NULL THEN 1 END) AS MAPPED

    FROM MAPPING_ENTITY me JOIN MAPPING_VALUES mv on me.ENTITY_ID = mv.MAPPING_ENTITY_ID
    WHERE mv.MAPPING_VALUES_KEY = 'DataSource' AND ENTITY_TYPE='treatment'  GROUP BY lower(MAPPING_VALUES)
     */
    @Query("SELECT distinct lower(mv), count(case when me.mapType is null THEN 1 END), " +
            " count(case when me.mapType is not null THEN 1 END) from MappingEntity me join me.mappingValues mv " +
            " WHERE KEY(mv) = 'DataSource' " +
            " AND ((lower(me.entityType) = lower(:entityType)) OR :entityType = '' ) group by lower(mv)")
    List<Object[]> findMissingMappingStat(@Param("entityType") String entityType);


}