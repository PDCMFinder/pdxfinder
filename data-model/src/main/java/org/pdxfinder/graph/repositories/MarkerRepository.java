package org.pdxfinder.graph.repositories;

import org.pdxfinder.graph.dao.Marker;
import org.pdxfinder.graph.dao.MarkerAssociation;
import org.pdxfinder.graph.queryresults.MutatedMarkerData;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Interface for Markers
 */
@Repository
public interface MarkerRepository extends PagingAndSortingRepository<Marker, Long> {

    @Query("MATCH (t:Marker) WHERE t.hgncSymbol = {symbol} RETURN t")
    Marker findBySymbol(@Param("symbol") String symbol);

    @Query("MATCH (t:Marker) WHERE t.name = {name} RETURN t")
    Marker findByName(@Param("name") String name);
    
    @Query("MATCH (t:Marker) WHERE t.ensembleId = {id} RETURN t")
    Marker findByEnsemblId(@Param("id") String id);

    @Query("MATCH (m:Marker) RETURN m")
    Collection<Marker> findAllMarkers();

    @Query("MATCH (m:Marker) " +
            "RETURN m")
    Collection<Marker> findAllHumanMarkers();

    @Query("MATCH (s:Sample)--(:MolecularCharacterization)--(:MarkerAssociation)--(m:Marker) WHERE s.sourceSampleId = {sampleId}  return m")
    Collection<Marker> findAllBySampleId(@Param("sampleId") String sampleId);

    @Query("MATCH (ma:MarkerAssociation)--(m:Marker) WHERE id(ma) = {ma} RETURN m")
    Marker findByMarkerAssociation(@Param("ma") MarkerAssociation ma);



    @Query("MATCH (mod:ModelCreation)--(spec:Specimen)--(msamp:Sample)--(molchar:MolecularCharacterization)-->(mAss:MarkerAssociation)--(marker:Marker) " +
            "RETURN marker.hgncSymbol AS gene_name, COUNT(DISTINCT mod.sourcePdxId) as number_of_models " +
            "ORDER BY number_of_models DESC")
    List<MutatedMarkerData> countModelsByMarker();


    @Query("MATCH (m:Marker) WHERE {synonym} IN m.aliasSymbols RETURN m")
    List<Marker> findBySynonym(@Param("synonym") String synonym);

    @Query("MATCH (m:Marker) WHERE {synonym} IN m.prevSymbols RETURN m")
    List<Marker> findByPrevSymbol(@Param("synonym") String synonym);

    @Query("MATCH (m:Marker) WHERE NOT (m)--() DELETE m")
    void deleteMarkersWithoutRelationships();

    @Query("MATCH (mc:MolecularCharacterization)-[awr:ASSOCIATED_WITH]-(ma:MarkerAssociation)-[mr:MARKER]-(m:Marker) " +
            "WHERE ID(mc) = {id} " +
            "RETURN DISTINCT m")
    Set<Marker> findDistinctByMolCharId(@Param("id") Long id);

}
