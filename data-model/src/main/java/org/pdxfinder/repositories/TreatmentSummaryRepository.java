package org.pdxfinder.repositories;

import org.pdxfinder.dao.TreatmentSummary;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing creating, finding, and deleting tissues
 */
@Repository
public interface TreatmentSummaryRepository extends Neo4jRepository<TreatmentSummary, Long> {
}
