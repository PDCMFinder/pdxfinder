package org.pdxfinder.repositories;

import org.pdxfinder.dao.ImplantationSite;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

/**
 * Interface for implantation site records
 */
@Repository
public interface ImplantationSiteRepository extends Neo4jRepository<ImplantationSite, Long> {

    @Query("MATCH (t:ImplantationSite) WHERE t.name = {0} RETURN t")
    ImplantationSite findByName(String name);

}
