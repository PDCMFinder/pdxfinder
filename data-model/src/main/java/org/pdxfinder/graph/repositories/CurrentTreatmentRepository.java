package org.pdxfinder.graph.repositories;

import org.pdxfinder.graph.dao.CurrentTreatment;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

/*
 * Created by csaba on 25/07/2018.
 */
@Repository
public interface CurrentTreatmentRepository extends Neo4jRepository<CurrentTreatment, Long>{


    CurrentTreatment findByName(String name);
}
