package org.pdxfinder.repositories;

import org.pdxfinder.dao.ExternalUrl;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExternalUrlRepository extends PagingAndSortingRepository<ExternalUrl, Long> {


    @Query("MATCH (u:ExternalUrl) WHERE u.type = {type} AND u.url = {url} RETURN u")
    ExternalUrl findByTypeAndUrl(@Param("type") String type, @Param("url") String url);

    @Query("MATCH (exturl:ExternalUrl) RETURN DISTINCT exturl.type ORDER BY exturl.type")
    List<String> findAllTypes();

    @Query("MATCH (exturl:ExternalUrl) RETURN DISTINCT exturl.url ORDER BY exturl.url")
    List<String> findAllUrls();

}