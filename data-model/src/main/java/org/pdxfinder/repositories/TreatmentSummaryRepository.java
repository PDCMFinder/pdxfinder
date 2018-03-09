package org.pdxfinder.repositories;

import org.pdxfinder.dao.TreatmentSummary;
import org.springframework.data.neo4j.repository.Neo4jRepository;

/**
 * Repository for managing creating, finding, and deleting tissues
 */
public interface TreatmentSummaryRepository extends Neo4jRepository<TreatmentSummary, Long> {
}
