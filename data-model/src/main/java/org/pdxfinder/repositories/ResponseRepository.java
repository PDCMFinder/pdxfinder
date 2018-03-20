package org.pdxfinder.repositories;

import org.neo4j.ogm.cypher.query.PagingAndSorting;
import org.pdxfinder.dao.Response;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

/*
 * Created by csaba on 20/10/2017.
 */
@Repository
public interface ResponseRepository extends PagingAndSortingRepository<Response, Long>{

}
