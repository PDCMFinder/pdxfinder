package org.pdxfinder.graph.repositories;

import org.pdxfinder.graph.dao.TreatmentProtocol;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;


/*
 * Created by csaba on 23/10/2017.
 */
@Repository
public interface TreatmentProtocolRepository extends Neo4jRepository<TreatmentProtocol, Long> {


}
