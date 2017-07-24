package org.pdxfinder.repositories;

import org.pdxfinder.dao.PlatformAssociation;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.Set;

/**
 * Created by jmason on 21/07/2017.
 */
public interface PlatformAssociationRepository extends PagingAndSortingRepository<PlatformAssociation, Long> {

    Set<PlatformAssociation> findAllByPlatform_Name(@Param("name") String name);

    @Query("MATCH (pa:PlatformAssociation), (p:Platform), (ds:ExternalDataSource) WHERE (pa)--(p)--(ds) AND ds.name = {name} RETURN pa")
    Set<PlatformAssociation> findByPlatform_ExternalDataSource_Name(@Param("name") String name);

}
