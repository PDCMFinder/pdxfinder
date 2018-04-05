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

    Set<Platform> findAllByExternalDataSource_Name(@Param("name") String name);

    Platform findByName(@Param("name") String name);

    @Query("MATCH (p:Platform)-[]-(d:ExternalDataSource) where p.name = {name} and d.name={dataSource} return p")
    Platform findByNameAndDataSource(@Param("name") String name, @Param("dataSource") String dataSource);

    @Query("MATCH (p:Platform)-[]-(d:ExternalDataSource) where p.name = {name} and d.name={dataSource} and p.url = {url} return p")
    Platform findByNameAndDataSourceAndUrl(@Param("name") String name, @Param("dataSource") String dataSource, @Param("url") String url);



    @Query("MATCH (mod:ModelCreation)--(spec:Specimen)--(msamp:Sample)--(molchar:MolecularCharacterization)-->(plat:Platform) " +
            "WHERE mod.dataSource = {dataSource} AND mod.sourcePdxId={modelId} " +
            "RETURN distinct plat")
    List<Platform> findModelPlatformByModelId(@Param("dataSource") String dataSource, @Param("modelId") String modelId);


    @Query("MATCH (plat:Platform)--(src:ExternalDataSource) " +
            "WHERE toLower(src.abbreviation)=toLower({dataSource}) " +
            "RETURN plat ")
    List<Platform> findPlatformByExternalDataSource(@Param("dataSource") String dataSource);


    @Query("MATCH (molChar:MolecularCharacterization)-[pus:PLATFORM_USED]-(p:Platform)-[assW:ASSOCIATED_WITH]-(eds:ExternalDataSource) " +
            "WHERE p.name={platform} AND toLower(eds.abbreviation)=toLower({dataSource}) " +

            "WITH molChar, pus, p " +
            "OPTIONAL MATCH (molChar)-[assW:ASSOCIATED_WITH]-(mAss:MarkerAssociation) " +
            "RETURN count(mAss)")
    int countMarkerAssocByPlatformAndExternalDataSource(@Param("platform") String platform, @Param("dataSource") String dataSource);

    @Query("MATCH (mod:ModelCreation)--(s:Sample)--(molChar:MolecularCharacterization)-[pus:PLATFORM_USED]-(p:Platform) " +
            "WHERE p.name={platform} AND toLower(mod.dataSource)=toLower({dataSource}) " +
            "RETURN count(distinct mod)")
    int countModelsByPlatformAndExternalDataSource(@Param("platform") String platform, @Param("dataSource") String dataSource);


    @Query("MATCH (pl:Platform) WHERE EXISTS(pl.url) RETURN pl")
    Collection<Platform> findAllWithUrl();


}
