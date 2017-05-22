package org.pdxfinder.repositories;

import org.pdxfinder.dao.Patient;
import org.pdxfinder.dao.PatientSnapshot;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface PatientSnapshotRepository extends Neo4jRepository<PatientSnapshot, Long> {

    Set<Patient> findByAge(String sex);

    Set<Patient> findByPatientSex(String sex);

    Patient findByPatientExternalId(String externalId);
    
    Set<PatientSnapshot> findByPatient(Patient patient);

    @Query("MATCH (s:Sample)--(ps:PatientSnapshot)" +
            "WHERE s.sourceSampleId = {sampleId} " +
            "RETURN ps")
    PatientSnapshot findBySampleId(@Param("sampleId") String sampleId);

}
