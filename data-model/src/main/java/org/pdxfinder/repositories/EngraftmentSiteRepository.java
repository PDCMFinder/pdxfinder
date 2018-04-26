package org.pdxfinder.repositories;

import org.pdxfinder.dao.EngraftmentSite;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

/**
 * Interface for implantation site records
 */
@Repository
public interface EngraftmentSiteRepository extends Neo4jRepository<EngraftmentSite, Long> {

    @Query("MATCH (t:EngraftmentSite) WHERE t.name = {0} RETURN t")
    EngraftmentSite findByName(String name);

}
