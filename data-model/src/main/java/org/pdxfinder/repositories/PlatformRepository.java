package org.pdxfinder.repositories;

import org.pdxfinder.dao.Platform;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.Set;

/**
 * Created by jmason on 21/07/2017.
 */
public interface PlatformRepository extends PagingAndSortingRepository<Platform, Long> {

    Set<Platform> findAllByExternalDataSource_Name(@Param("name") String name);

    Platform findByName(@Param("name") String name);

    @Query("MATCH (p:Platform)-[]-(d:ExternalDataSource) where p.name = {name} and d.name={dataSource} return p")
    Platform findByNameAndDataSource(@Param("name") String name, @Param("dataSource") String dataSource);
    

}
