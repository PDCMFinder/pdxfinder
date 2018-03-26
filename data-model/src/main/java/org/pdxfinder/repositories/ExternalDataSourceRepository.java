package org.pdxfinder.repositories;

import org.pdxfinder.dao.ExternalDataSource;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Interface to the sources of data in PDX finder
 */
@Repository
public interface ExternalDataSourceRepository extends PagingAndSortingRepository<ExternalDataSource, Long> {

    ExternalDataSource findByName(@Param("name") String dataSourceName);

    ExternalDataSource findByAbbreviation(@Param("abbr") String dataSourceAbbr);

    @Query("MATCH (ed:ExternalDataSource) RETURN DISTINCT ed.abbreviation ORDER BY ed.abbreviation")
    List<String> findAllAbbreviations();

}
