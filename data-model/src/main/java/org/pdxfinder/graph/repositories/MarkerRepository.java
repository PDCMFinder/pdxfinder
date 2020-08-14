package org.pdxfinder.graph.repositories;

import org.neo4j.ogm.model.Result;
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

    @Query("MATCH (t:Marker) WHERE t.ncbiGeneId = {id} return t")
    Marker findByNcbiGeneId(@Param("id") String id);

    @Query("MATCH (m:Marker) RETURN count(m)")
    Integer countAllMarkers();

    @Query("MATCH (m:Marker) " +
            "RETURN m")
    Collection<Marker> findAllHumanMarkers();

    @Query("MATCH (m:Marker) return count(m)")
    int getMarkerCount();

    @Query("MATCH (m:Marker) return m SKIP {skip} LIMIT {limit}")
    Collection<Marker> getAllMarkersSkipLimit(@Param("skip") int skip, @Param("limit") int limit);


}
