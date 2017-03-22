package org.pdxfinder.repositories;

import org.pdxfinder.dao.Sample;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.Set;

/**
 * Repository for managing creating, finding, and deleting sample objects
 */
public interface SampleRepository extends Neo4jRepository<Sample, Long> {

    Sample findBySourceSampleId(String sourceSampleId);

    Set<Sample> findByDataSource(String dataSource);

    Set<Sample> findByExternalDataSourceAbbreviation(String abbreviation);

}
