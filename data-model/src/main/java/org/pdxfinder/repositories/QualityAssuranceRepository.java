package org.pdxfinder.repositories;

import org.pdxfinder.dao.QualityAssurance;
import org.pdxfinder.dao.ValidationTechniques;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface QualityAssuranceRepository extends Neo4jRepository<QualityAssurance, Long> {

    QualityAssurance findFirstByTechnologyAndDescriptionAndValidationTechniques(@Param("technology") String technology, @Param("description") String description, @Param("technique") ValidationTechniques technique);

}
