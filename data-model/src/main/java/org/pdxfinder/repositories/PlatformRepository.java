package org.pdxfinder.repositories;

import org.pdxfinder.dao.Platform;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.Set;

/**
 * Created by jmason on 21/07/2017.
 */
public interface PlatformRepository extends PagingAndSortingRepository<Platform, Long> {

    Set<Platform> findAllByExternalDataSource_Name(@Param("name") String name);

    Platform findByName(@Param("name") String name);

}
