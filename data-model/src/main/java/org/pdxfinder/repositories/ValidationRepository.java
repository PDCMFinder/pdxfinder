package org.pdxfinder.repositories;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import org.pdxfinder.dao.Validation;


@Repository
public interface ValidationRepository extends Neo4jRepository<Validation, Long> {


}
