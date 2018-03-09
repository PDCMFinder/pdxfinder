package org.pdxfinder.repositories;


import org.pdxfinder.dao.DataProjection;
import org.springframework.data.neo4j.repository.Neo4jRepository;

/*
 * Created by csaba on 09/03/2018.
 */
public interface DataProjectionRepository extends Neo4jRepository<DataProjection, Long> {


}
