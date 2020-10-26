package org.pdxfinder.graph.repositories;

import org.pdxfinder.graph.dao.ExternalUrl;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface ExternalUrlRepository extends PagingAndSortingRepository<ExternalUrl, Long> {


    @Query("MATCH (u:ExternalUrl) WHERE u.type = {type} AND u.url = {url} RETURN u")
    ExternalUrl findByTypeAndUrl(@Param("type") String type, @Param("url") String url);

}