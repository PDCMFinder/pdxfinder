package org.pdxfinder.repositories;

import org.pdxfinder.dao.Tumor;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.Set;

/**
 * Repository for managing creating, finding, and deleting tumor objects
 */
public interface TumorRepository extends Neo4jRepository<Tumor, Long> {

    Tumor findBySourceTumorId(String sourceTumorId);

    Set<Tumor> findByDataSource(String dataSource);

    Set<Tumor> findByExternalDataSourceAbbreviation(String abbreviation);

}
