package org.pdxfinder.repositories;

import org.pdxfinder.dao.TumorType;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;

/**
 * Interface for the Tumor Type repository
 */
public interface TumorTypeRepository extends Neo4jRepository<TumorType, Long> {

    @Query("MATCH (t:TumorType) WHERE t.name = {0} RETURN t")
    TumorType findByName(String name);

}
