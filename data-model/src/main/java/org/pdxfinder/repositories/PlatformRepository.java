package org.pdxfinder.repositories;

import org.pdxfinder.dao.Platform;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by jmason on 21/07/2017.
 */
@Repository
public interface PlatformRepository extends PagingAndSortingRepository<Platform, Long> {

    Platform findByName(@Param("name") String name);

    @Query("MATCH (p:Platform)-[]-(g:Group) where p.name = {name} and g.name={dataSource} and g.type='Provider' return p")
    Platform findByNameAndDataSource(@Param("name") String name, @Param("dataSource") String dataSource);

    @Query("MATCH (p:Platform)-[]-(g:Group) where p.name = {name} and g.name={dataSource} and g.type='Provider' and p.url = {url} return p")
    Platform findByNameAndDataSourceAndUrl(@Param("name") String name, @Param("dataSource") String dataSource, @Param("url") String url);



    @Query("MATCH (mod:ModelCreation)--(spec:Specimen)--(msamp:Sample)--(molchar:MolecularCharacterization)-->(plat:Platform) " +
            "WHERE mod.dataSource = {dataSource} AND mod.sourcePdxId={modelId} " +
            "RETURN distinct plat")
    List<Platform> findModelPlatformByModelId(@Param("dataSource") String dataSource, @Param("modelId") String modelId);


    @Query("MATCH (plat:Platform)--(src:Group) " +
            "WHERE toLower(src.abbreviation)=toLower({dataSource}) " +
            "RETURN plat ")
    List<Platform> findPlatformByExternalDataSource(@Param("dataSource") String dataSource);


    @Query("MATCH (mod:ModelCreation)--(s:Sample)--(molChar:MolecularCharacterization)-[pus:PLATFORM_USED]-(p:Platform) " +
            "WHERE p.name={platform} AND toLower(mod.dataSource)=toLower({dataSource}) " +
            "RETURN count(distinct mod)")
    int countModelsByPlatformAndExternalDataSource(@Param("platform") String platform, @Param("dataSource") String dataSource);


    @Query("MATCH (pl:Platform) WHERE EXISTS(pl.url) RETURN pl")
    Collection<Platform> findAllWithUrl();


}
