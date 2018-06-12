package org.pdxfinder.repositories;

import org.pdxfinder.dao.TreatmentComponent;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

/*
 * Created by csaba on 23/05/2018.
 */
@Repository
public interface TreatmentComponentRepository extends Neo4jRepository<TreatmentComponent, Long>{


}
