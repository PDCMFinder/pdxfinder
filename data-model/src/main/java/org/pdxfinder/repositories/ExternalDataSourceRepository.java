package org.pdxfinder.repositories;

import org.pdxfinder.dao.ExternalDataSource;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

/**
 * Interface to the sources of data in PDX finder
 */
public interface ExternalDataSourceRepository extends PagingAndSortingRepository<ExternalDataSource, Long> {

    ExternalDataSource findByName(@Param("name") String dataSourceName);

    ExternalDataSource findByAbbreviation(@Param("abbr") String dataSourceAbbr);

}
