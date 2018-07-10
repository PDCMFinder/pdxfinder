package org.pdxfinder.repositories;

import org.neo4j.ogm.model.Result;
import org.pdxfinder.dao.Marker;
import org.pdxfinder.dao.MarkerAssociation;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;

/**
 * Interface for Markers
 */
@Repository
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

    @Query("MATCH (ma:MarkerAssociation)--(m:Marker) WHERE id(ma) = {ma} RETURN m")
    Marker findByMarkerAssociation(@Param("ma") MarkerAssociation ma);



    @Query("MATCH (mod:ModelCreation)--(spec:Specimen)--(msamp:Sample)--(molchar:MolecularCharacterization)-->(mAss:MarkerAssociation)--(marker:Marker) " +
            "RETURN marker.name AS gene_name, COUNT(DISTINCT mod.sourcePdxId) as number_of_models " +
            "ORDER BY number_of_models DESC")
    Result countModelsByMarker();

}
