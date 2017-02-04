package org.pdxfinder.repositories;

import org.pdxfinder.dao.BackgroundStrain;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;

/**
 * Interface for implantation site records
 */
public interface BackgroundStrainRepository extends Neo4jRepository<BackgroundStrain, Long> {

    @Query("MATCH (t:BackgroundStrain) WHERE t.name = {0} RETURN t")
    BackgroundStrain findByName(String name);

}
