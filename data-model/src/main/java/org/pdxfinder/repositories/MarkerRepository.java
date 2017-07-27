package org.pdxfinder.repositories;

import org.pdxfinder.dao.Marker;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

/**
 * Interface for Markers
 */
public interface MarkerRepository extends PagingAndSortingRepository<Marker, Long> {

    @Query("MATCH (t:Marker) WHERE t.symbol = {symbol} RETURN t")
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

}
