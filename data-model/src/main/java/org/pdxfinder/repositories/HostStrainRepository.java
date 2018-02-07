package org.pdxfinder.repositories;

import org.pdxfinder.dao.HostStrain;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

/**
 * Interface for implantation site records
 */
public interface HostStrainRepository extends PagingAndSortingRepository<HostStrain, Long> {

    @Query("MATCH (t:HostStrain) WHERE t.symbol = {symbol} RETURN t")
    HostStrain findBySymbol(@Param("symbol") String symbol);

    @Query("MATCH (t:HostStrain) WHERE t.name = {name} RETURN t")
    HostStrain findByName(@Param("name") String name);

    @Query("MATCH (t:HostStrain) WHERE t.name = {name} AND t.symbol = {symbol} RETURN t")
    HostStrain findByNameAndSymbol(@Param("name") String name, @Param("symbol") String symbol);

}
