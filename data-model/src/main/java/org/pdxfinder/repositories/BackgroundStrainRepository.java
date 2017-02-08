package org.pdxfinder.repositories;

import org.pdxfinder.dao.BackgroundStrain;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

/**
 * Interface for implantation site records
 */
public interface BackgroundStrainRepository extends PagingAndSortingRepository<BackgroundStrain, Long> {

    @Query("MATCH (t:BackgroundStrain) WHERE t.symbol = {symbol} RETURN t")
    BackgroundStrain findBySymbol(@Param("symbol") String symbol);

    @Query("MATCH (t:BackgroundStrain) WHERE t.name = {name} RETURN t")
    BackgroundStrain findByName(@Param("name") String name);

}
