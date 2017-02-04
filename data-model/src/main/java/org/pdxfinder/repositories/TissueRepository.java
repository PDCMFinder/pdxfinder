package org.pdxfinder.repositories;

import org.pdxfinder.dao.Tissue;
import org.springframework.data.neo4j.repository.Neo4jRepository;

/**
 * Repository for managing creating, finding, and deleting tissues
 */
public interface TissueRepository extends Neo4jRepository<Tissue, Long> {
    Tissue findByName(String name);
}
