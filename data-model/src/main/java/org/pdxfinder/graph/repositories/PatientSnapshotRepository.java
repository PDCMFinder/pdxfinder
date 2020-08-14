package org.pdxfinder.graph.repositories;

import org.pdxfinder.graph.dao.PatientSnapshot;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface PatientSnapshotRepository extends Neo4jRepository<PatientSnapshot, Long> {

    @Query("MATCH (ps:PatientSnapshot)--(p:Patient) WHERE p.externalId = {patientId} RETURN ps")
    Set<PatientSnapshot> findByPatient(@Param("patientId") String patientId);

    @Query("MATCH (p:Patient)--(ps:PatientSnapshot) " +
            "WHERE p.externalId = {patientId} " +
            "AND p.dataSource = {dataSource} " +
            "AND ps.ageAtCollection = {age} " +
            "RETURN ps")
    PatientSnapshot findByPatientIdAndDataSourceAndAge(@Param("patientId") String patientId,
                                                       @Param("dataSource") String dataSource,
                                                       @Param("age") String age);

}
