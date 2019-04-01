package org.pdxfinder.graph.repositories;

import org.pdxfinder.graph.dao.QualityAssurance;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface QualityAssuranceRepository extends Neo4jRepository<QualityAssurance, Long> {

    QualityAssurance findFirstByTechnologyAndDescription(@Param("technology") String technology, @Param("description") String description);

}
