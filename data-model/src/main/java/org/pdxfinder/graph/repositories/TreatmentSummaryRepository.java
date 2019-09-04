package org.pdxfinder.graph.repositories;

import org.pdxfinder.graph.dao.PatientSnapshot;
import org.pdxfinder.graph.dao.TreatmentSummary;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing creating, finding, and deleting tissues
 */
@Repository
public interface TreatmentSummaryRepository extends Neo4jRepository<TreatmentSummary, Long> {

    @Query("MATCH (mod:ModelCreation)--(ts:TreatmentSummary) WHERE mod.dataSource = {dataSource} AND mod.sourcePdxId = {modelId} " +
            "WITH ts " +
            "MATCH (ts)-[tpr:TREATMENT_PROTOCOL]-(tp:TreatmentProtocol)-[rr:RESPONSE]-(r:Response) " +
            "MATCH (tp)-[tcr:TREATMENT_COMPONENT]-(tc:TreatmentComponent)-[dr:TREATMENT]-(d:Treatment)-[mtr:MAPPED_TO]-(ot:OntologyTerm) "+
            "RETURN ts, tpr, tp, rr, r, tcr, tc, dr, d, mtr, ot")
    TreatmentSummary findModelTreatmentByDataSourceAndModelId(@Param("dataSource") String dataSource, @Param("modelId") String modelId);

    @Query("MATCH (mod:ModelCreation)--(s:Sample)--(ps:PatientSnapshot)--(ts:TreatmentSummary) WHERE mod.dataSource = {dataSource} AND mod.sourcePdxId = {modelId} " +
            "WITH ts " +
            "MATCH (ts)-[tpr:TREATMENT_PROTOCOL]-(tp:TreatmentProtocol)-[rr:RESPONSE]-(r:Response) " +
            "MATCH (tp)-[tcr:TREATMENT_COMPONENT]-(tc:TreatmentComponent)-[dr:TREATMENT]-(d:Treatment) "+
            "RETURN ts, tpr, tp, rr, r, tcr, tc, dr, d")
    TreatmentSummary findPatientTreatmentByDataSourceAndModelId(@Param("dataSource") String dataSource, @Param("modelId") String modelId);

    @Query("MATCH (mod:ModelCreation)--(ts:TreatmentSummary) WHERE toLower(mod.dataSource) = toLower({dataSource}) RETURN count(distinct mod)")
    int findDrugDosingStudyNumberByDataSource(@Param("dataSource") String dataSource);

    @Query("MATCH (mod:ModelCreation)--(s:Sample)--(ps:PatientSnapshot)--(ts:TreatmentSummary) WHERE toLower(mod.dataSource) = toLower({dataSource}) RETURN count(distinct mod)")
    int findPatientTreatmentNumberByDataSource(@Param("dataSource") String dataSource);


    @Query("MATCH (mod:ModelCreation)--(ts:TreatmentSummary) WHERE toLower(mod.dataSource) = toLower({dataSource}) AND EXISTS(ts.url) RETURN ts.url LIMIT 1")
    String findPlatformUrlByDataSource(@Param("dataSource") String dataSource);

    @Query("MATCH (ts:TreatmentSummary)-[tpr:TREATMENT_PROTOCOL]-(tp:TreatmentProtocol)-[tcr:TREATMENT_COMPONENT]-(tc:TreatmentComponent)-[dr:TREATMENT]-(d:Treatment) " +
            "MATCH (tp)-[rr:RESPONSE]-(r:Response) " +
            "RETURN ts, tpr, tp, tcr, tc, dr, d, rr, r")
    List<TreatmentSummary> findAllWithDrugData();

    @Query("MATCH (mod:ModelCreation)--(ts:TreatmentSummary)-[tpr:TREATMENT_PROTOCOL]-(tp:TreatmentProtocol)-[tcr:TREATMENT_COMPONENT]-(tc:TreatmentComponent)-[dr:TREATMENT]-(d:Treatment)-[mtr:MAPPED_TO]-(ot:OntologyTerm) " +
            "MATCH (tp)-[rr:RESPONSE]-(r:Response) " +
            "OPTIONAL MATCH (ot)-[sor:SUBCLASS_OF]-(ot2:OntologyTerm) " +
            "RETURN ts, tpr, tp, tcr, tc, dr, d, rr, r, mtr, ot, sor, ot2")
    List<TreatmentSummary> findAllMouseTreatments();

    @Query("MATCH (ps:PatientSnapshot)--(ts:TreatmentSummary)-[tpr:TREATMENT_PROTOCOL]-(tp:TreatmentProtocol)-[tcr:TREATMENT_COMPONENT]-(tc:TreatmentComponent)-[dr:TREATMENT]-(d:Treatment) " +
            "MATCH (tp)-[rr:RESPONSE]-(r:Response) " +
            "RETURN ts, tpr, tp, tcr, tc, dr, d, rr, r")
    List<TreatmentSummary> findAllPatientTreatments();

    @Query("MATCH (ts:TreatmentSummary) RETURN count(ts)")
    int findTotalSummaryNumber();


    @Query("MATCH (ts:TreatmentSummary)--(ps:PatientSnapshot) WHERE id(ps) = {snapshot} " +
            "WITH ts " +
            "OPTIONAL MATCH (ts)-[tpr:TREATMENT_PROTOCOL]-(tp:TreatmentProtocol)-[rr:RESPONSE]-(r:Response) " +
            "OPTIONAL MATCH (tp)-[tcr:TREATMENT_COMPONENT]-(tc:TreatmentComponent)-[dr:Treatment]-(d:Treatment) "+
            "RETURN ts, tpr, tp, rr, r, tcr, tc, dr, d")
    TreatmentSummary findByPatientSnapshot(@Param("snapshot")PatientSnapshot ps);



}
