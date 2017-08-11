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

    @Query("MATCH (ps:PatientSnapshot)--(p:Patient) WHERE p.externalId = {patientId} RETURN ps")
    Set<PatientSnapshot> findByPatient(@Param("patientId") String patientId);

    @Query("MATCH (mod:ModelCreation)-[ii:IMPLANTED_IN]-(s:Sample)-[sf:SAMPLED_FROM]-(ps:PatientSnapshot)-[pr:PATIENT]-(p:Patient)" +
            "WHERE mod.sourcePdxId = {modelId} " +
            "RETURN mod,ii,s,sf,ps, p, pr")
    PatientSnapshot findByModelId(@Param("modelId") String modelId);

}
