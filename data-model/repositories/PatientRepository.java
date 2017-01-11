package org.pdxi.repositories;

import org.pdxi.dao.Patient;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface PatientRepository extends GraphRepository<Patient> {

    Set<Patient> findBySex(String sex);
    Set<Patient> findBySexAndAge(String sex, String age);
    Patient findByExternalId(String externalId);

}
