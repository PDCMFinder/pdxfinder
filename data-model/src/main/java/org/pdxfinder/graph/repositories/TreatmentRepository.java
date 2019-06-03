package org.pdxfinder.graph.repositories;

import org.pdxfinder.graph.dao.Treatment;
import org.springframework.data.neo4j.repository.Neo4jRepository;

/*
 * Created by csaba on 08/05/2019.
 */
public interface TreatmentRepository extends Neo4jRepository<Treatment, Long> {


}
