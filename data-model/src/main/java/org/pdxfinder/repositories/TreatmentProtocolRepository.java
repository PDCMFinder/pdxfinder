package org.pdxfinder.repositories;

import org.pdxfinder.dao.TreatmentProtocol;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 * Created by csaba on 23/10/2017.
 */
@Repository
public interface TreatmentProtocolRepository extends Neo4jRepository<TreatmentProtocol, Long> {



    @Query("MATCH (tp:TreatmentProtocol) RETURN DISTINCT tp.drug")
    List<List<String>> findDrugNames();




}
