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

    @Query("MATCH (mod:ModelCreation)-[ii:IMPLANTED_IN]-(s:Sample) " +
            "MATCH (s:Sample)-[sf:SAMPLED_FROM]-(ps:PatientSnapshot)-[pt:PATIENT]-(p:Patient) " +
            "WHERE mod.sourcePdxId = {modelId} " +
            "RETURN mod, ii, s, ps, p, sf, pt")
    Patient findByModelId(@Param("modelId") String modelId);

    @Query("MATCH (mod:ModelCreation)-[ii:IMPLANTED_IN]-(s:Sample) " +
            "MATCH (s:Sample)--(ps:PatientSnapshot)--(p:Patient) " +
            "WHERE s.sourceSampleId = {sampleId} " +
            "RETURN mod, ii, s, ps, p")
    Patient findBySampleId(@Param("sampleId") String sampleId);


    @Query("MATCH (mod:ModelCreation)-[ii:IMPLANTED_IN]-(s:Sample) " +
            "MATCH (s:Sample)-[sf:SAMPLED_FROM]-(ps:PatientSnapshot)-[pt:PATIENT]-(p:Patient) " +
            "WHERE mod.sourcePdxId = {modelId} " +
            "AND toLower(s.dataSource) = toLower({dataSource}) "+
            "RETURN mod, ii, s, ps, p, sf, pt")
    Patient findByDataSourceAndModelId(@Param("dataSource") String dataSource, @Param("modelId") String modelId);


}
