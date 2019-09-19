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

    Optional<MappingEntity> findByMappingKeyAndEntityId(String mappingKey, Long entityId);

    List<MappingEntity> findByMappedTermLabel(String mappedTermLabel);


    @Query(value = "Select distinct me from MappingEntity me JOIN me.mappingValues mv " +
            "WHERE ((lower(me.entityType) in :entityType) OR :entityType = '0') "+
            "AND ( (lower(KEY(mv)) = lower(:mappingLabel) AND lower(mv) in :mappingValue) OR  :mappingValue = '0' ) "+
            "AND ((lower(me.mappedTermLabel) = lower(:mappedTermLabel)) OR :mappedTermLabel = '') "+

            "AND ((lower(me.mapType) = lower(:mapType)) OR :mapType = '') "+
            "AND ((lower(me.status) in :status) OR :status = '0') "+

            "AND ( :mappedTermsOnly = '' OR (me.mapType is not null )) "
    )
    Page<MappingEntity> findByMultipleFilters(@Param("entityType") List<String> entityType,
                                              @Param("mappingLabel") String mappingLabel,
                                              @Param("mappingValue") List<String> mappingValue,
                                              @Param("mappedTermLabel") String mappedTermLabel,
                                              @Param("mapType") String mapType,
                                              @Param("mappedTermsOnly") String mappedTermsOnly,
                                              @Param("status") List<String> status,

                                              Pageable pageable);


    @Query(value = "select me from MappingEntity me JOIN me.mappingValues mv WHERE KEY(mv) = :dataKey AND mv = :dataValue ")
    List<MappingEntity> findByAttributeAndValue(@Param("dataKey") String dataKey,
                                                @Param("dataValue") String dataValue);


    List<MappingEntity> findByEntityTypeAndMapTypeIsNotNull(String entityType);

    List<MappingEntity> findByEntityTypeAndStatusIsNot(String entityType, String statusIsNot);




    /*
    Query:
    SELECT DISTINCT lower(MAPPING_VALUES),
                    COUNT(CASE WHEN STATUS = 'unmapped' THEN 1 END) AS UNMAPPED,
                    COUNT(CASE WHEN STATUS <> 'unmapped' AND STATUS <> 'orphaned' THEN 1 END) AS MAPPED,
                    COUNT(CASE WHEN STATUS = 'validated' THEN 1 END) AS VALIDATED,
                    COUNT(CASE WHEN STATUS = 'created' THEN 1 END) AS UNVALIDATED,
                    COUNT(CASE WHEN STATUS = 'orphaned' THEN 1 END) AS ORPHANED

    FROM MAPPING_ENTITY me JOIN MAPPING_VALUES mv on me.ENTITY_ID = mv.MAPPING_ENTITY_ID
    WHERE mv.MAPPING_VALUES_KEY = 'DataSource' AND ENTITY_TYPE='treatment'  GROUP BY lower(MAPPING_VALUES) ORDER BY UNMAPPED desc

     */
    @Query("SELECT distinct lower(mv), " +
            " count(case when me.status = 'unmapped' THEN 1 END) as unmapped, " +
            " count(case when me.status <> 'unmapped' AND me.status <> 'orphaned'  THEN 1 END) as mapped, " +
            " count(case when me.status = 'validated' THEN 1 END) as validated, " +
            " count(case when me.status = 'created' THEN 1 END) as created, " +
            " count(case when me.status = 'orphaned' THEN 1 END) as orphaned " +
            " from MappingEntity me join me.mappingValues mv " +
            " WHERE KEY(mv) = 'DataSource' " +
            " AND ((lower(me.entityType) = lower(:entityType)) OR :entityType = '' ) group by lower(mv) order by unmapped desc ")
    List<Object[]> findMissingMappingStat(@Param("entityType") String entityType);


}