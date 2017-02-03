package org.pdxfinder.repositories;

import org.pdxfinder.dao.Patient;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface PatientRepository extends Neo4jRepository<Patient, Long> {

    Set<Patient> findBySex(String sex);

    Set<Patient> findBySexAndAge(String sex, String age);

    Patient findByExternalId(String externalId);

}
