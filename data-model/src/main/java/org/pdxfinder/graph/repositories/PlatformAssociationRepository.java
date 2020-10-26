package org.pdxfinder.graph.repositories;

import org.pdxfinder.graph.dao.PlatformAssociation;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * Created by jmason on 21/07/2017.
 */
@Repository
public interface PlatformAssociationRepository extends PagingAndSortingRepository<PlatformAssociation, Long> {

    @Query("MATCH (pa:PlatformAssociation), (p:Platform), (ds:Group) WHERE (pa)--(p)--(ds) AND ds.name = {name} AND ds.type='Provider' RETURN pa")
    Set<PlatformAssociation> findByPlatform_ExternalDataSource_Name(@Param("name") String name);

    @Query("MATCH (ds:Group), (pa:PlatformAssociation), (p:Platform), (m:Marker) WHERE (ds)--(p)--(pa)--(m) AND ds.name = {dsName} and p.name = {pName} and m.hgncSymbol = {mSymbol} AND ds.type='Provider' RETURN pa")
    PlatformAssociation findByPlatformAndMarker(@Param("pName") String pName, @Param("dsName") String dsName, @Param("mSymbol") String mSymbol);

}
