package org.pdxfinder.repositories;

import org.pdxfinder.dao.Tissue;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing creating, finding, and deleting tissues
 */
@Repository
public interface TissueRepository extends Neo4jRepository<Tissue, Long> {

    Tissue findByName(String name);
}
