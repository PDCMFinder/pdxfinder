package org.pdxfinder.repositories;

import org.pdxfinder.dao.Drug;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 * Created by csaba on 23/05/2018.
 */
@Repository
public interface DrugRepository extends Neo4jRepository<Drug, Long> {

    @Query("MATCH (d:Drug) RETURN DISTINCT d.name")
    List<String> findDistinctDrugNames();
}
