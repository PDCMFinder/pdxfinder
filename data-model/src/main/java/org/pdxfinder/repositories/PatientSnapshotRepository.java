package org.pdxfinder.repositories;

import org.pdxfinder.dao.Patient;
import org.pdxfinder.dao.PatientSnapshot;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface PatientSnapshotRepository extends Neo4jRepository<PatientSnapshot, Long> {

    Set<Patient> findByAge(String sex);

    Set<Patient> findByPatientSex(String sex);

    Patient findByPatientExternalId(String externalId);

    @Query("MATCH (ps:PatientSnapshot)--(p:Patient) WHERE p.externalId = {patientId} RETURN ps")
    Set<PatientSnapshot> findByPatient(@Param("patientId") String patientId);

    @Query("MATCH (mod:ModelCreation)-[ii:IMPLANTED_IN]-(s:Sample)-[sf:SAMPLED_FROM]-(ps:PatientSnapshot)-[pr:PATIENT]-(p:Patient) \n" +
            "WHERE mod.sourcePdxId = {modelId} " +
            "WITH mod, ii, s, sf, ps, pr, p " +
            "OPTIONAL MATCH (s)-[ot:ORIGIN_TISSUE]-(t1:Tissue) " +
            "OPTIONAL MATCH (s)-[ss:SAMPLE_SITE]-(t2:Tissue) " +

            "RETURN mod,ii,s,sf,ps, p, pr, t1, t2, ot, ss")
    PatientSnapshot findByModelId(@Param("modelId") String modelId);

    @Query("MATCH (mod:ModelCreation)-[ii:IMPLANTED_IN]-(s:Sample)-[sf:SAMPLED_FROM]-(ps:PatientSnapshot)-[pr:PATIENT]-(p:Patient) " +
            "WHERE mod.sourcePdxId = {modelId} " +
            "AND toLower(s.dataSource) = toLower({dataSource}) "+
            "WITH mod, ii, s, sf, ps, pr, p " +
            "OPTIONAL MATCH (s)-[ot:ORIGIN_TISSUE]-(t1:Tissue) " +
            "OPTIONAL MATCH (s)-[ss:SAMPLE_SITE]-(t2:Tissue) " +

            "RETURN mod,ii,s,sf,ps, p, pr, t1, t2, ot, ss")
    List<PatientSnapshot> findByDataSourceAndModelId(@Param("dataSource") String dataSource, @Param("modelId") String modelId);

    @Query("MATCH (p:Patient)--(ps:PatientSnapshot) " +
            "WHERE p.externalId = {patientId} " +
            "AND p.dataSource = {dataSource} " +
            "AND ps.age = {age} " +
            "RETURN ps")
    PatientSnapshot findByPatientIdAndDataSourceAndAge(@Param("patientId") String patientId,
                                                       @Param("dataSource") String dataSource,
                                                       @Param("age") String age);

}
