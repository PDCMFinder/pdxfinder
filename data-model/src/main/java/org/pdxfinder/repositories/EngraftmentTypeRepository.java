package org.pdxfinder.repositories;

import org.pdxfinder.dao.EngraftmentType;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

/**
 * Interface for implantation site records
 */
@Repository
public interface EngraftmentTypeRepository extends Neo4jRepository<EngraftmentType, Long> {

    @Query("MATCH (t:EngraftmentType) WHERE t.name = {0} RETURN t")
    EngraftmentType findByName(String name);

}
