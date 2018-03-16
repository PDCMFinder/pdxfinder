package org.pdxfinder.repositories;


import org.pdxfinder.dao.DataProjection;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/*
 * Created by csaba on 09/03/2018.
 */
@Repository
public interface DataProjectionRepository extends Neo4jRepository<DataProjection, Long> {


    @Query("MATCH (dp:DataProjection) WHERE dp.label = {label} return dp ")
    DataProjection findByLabel(@Param("label") String label);
}
