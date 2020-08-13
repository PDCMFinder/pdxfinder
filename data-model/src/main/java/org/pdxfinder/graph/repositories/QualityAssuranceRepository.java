package org.pdxfinder.graph.repositories;

import org.pdxfinder.graph.dao.QualityAssurance;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface QualityAssuranceRepository extends Neo4jRepository<QualityAssurance, Long> {

    @Query("MATCH (qa:QualityAssurance) WHERE qa.technology = {technology} AND qa.description = {description} RETURN qa")
    QualityAssurance findFirstByTechnologyAndDescription(@Param("technology") String technology, @Param("description") String description);

    @Query("MATCH (qa:QualityAssurance) WHERE qa.technology = {technology} AND qa.description = {description} AND qa.passage = {passage} RETURN qa")
    QualityAssurance findByTechnologyAndDescriptionAndPassage(@Param("technology") String technology, @Param("description") String description, @Param("passage") String passage);

}
