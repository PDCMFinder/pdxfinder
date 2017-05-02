package org.pdxfinder.repositories;

import org.pdxfinder.dao.Marker;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

/**
 * Interface for Markers
 */
public interface MarkerRepository extends PagingAndSortingRepository<Marker, Long> {

    @Query("MATCH (t:Marker) WHERE t.symbol = {symbol} RETURN t")
    Marker findBySymbol(@Param("symbol") String symbol);

    @Query("MATCH (t:Marker) WHERE t.name = {name} RETURN t")
    Marker findByName(@Param("name") String name);

}
