package org.pdxfinder.repositories;

import org.pdxfinder.dao.Specimen;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

/**
 * Created by jmason on 08/06/2017.
 */
public interface SpecimenRepository extends Neo4jRepository<Specimen, Long> {

    Specimen findByExternalId(@Param("externalId") String externalId);

}
