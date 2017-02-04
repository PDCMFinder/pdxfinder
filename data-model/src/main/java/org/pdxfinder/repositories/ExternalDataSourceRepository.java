package org.pdxfinder.repositories;

import org.pdxfinder.dao.ExternalDataSource;
import org.springframework.data.neo4j.repository.Neo4jRepository;

/**
 * Interface to the sources of data in PDX finder
 */
public interface ExternalDataSourceRepository extends Neo4jRepository<ExternalDataSource, Long> {

    ExternalDataSource findByName(String dataSourceName);

    ExternalDataSource findByAbbreviation(String dataSourceAbbr);

}
