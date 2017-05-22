package org.pdxfinder.repositories;

import org.pdxfinder.dao.Patient;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface PatientRepository extends Neo4jRepository<Patient, Long> {

    Set<Patient> findBySex(String sex);

    Set<Patient> findBySexAndSnapshotsAge(String sex, String age);

    Patient findByExternalId(String externalId);

    @Query("MATCH (s:Sample)--(ps:PatientSnapshot)--(p:Patient) " +
            "WHERE s.sourceSampleId = {sampleId} " +
            "RETURN s, ps, p")
    Patient findBySampleId(@Param("sampleId") String sampleId);

}
