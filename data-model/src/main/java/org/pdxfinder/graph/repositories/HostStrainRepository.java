package org.pdxfinder.graph.repositories;

import org.pdxfinder.graph.dao.HostStrain;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Interface for implantation site records
 */
@Repository
public interface HostStrainRepository extends PagingAndSortingRepository<HostStrain, Long> {

    @Query("MATCH (t:HostStrain) WHERE t.symbol = {symbol} RETURN t")
    HostStrain findBySymbol(@Param("symbol") String symbol);

    @Query("MATCH (t:HostStrain) WHERE t.name = {name} RETURN t")
    HostStrain findByName(@Param("name") String name);

}
