package org.pdxfinder.graph.repositories;

import org.pdxfinder.graph.dao.Sample;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Collection;

/**
 * Repository for managing creating, finding, and deleting sample objects
 */
@Repository
public interface SampleRepository extends PagingAndSortingRepository<Sample, Long> {

    @Query("MATCH (s:Sample) WHERE s.sourceSampleId = {sourceSampleId} AND s.dataSource = {dataSource} return s")
    Sample findBySourceSampleIdAndDataSource(@Param("sourceSampleId") String sourceSampleId, @Param("dataSource") String dataSource) ;

    @Query("MATCH (ps:PatientSnapshot)--(s:Sample) WHERE s.dataSource = {dataSource} AND s.sourceSampleId = {sampleId} RETURN s")
    Sample findHumanSampleBySampleIdAndDataSource(@Param("sampleId") String sampleId, @Param("dataSource") String dataSource);

    @Query("MATCH (ps:PatientSnapshot)--(s:Sample) RETURN count(s)")
    int findHumanSamplesNumber();

    @Query("MATCH (ps:PatientSnapshot)--(s:Sample) " +
            "WITH s " +
            "OPTIONAL MATCH (s)-[ot:ORIGIN_TISSUE]-(t:Tissue) " +
            "OPTIONAL MATCH (s)-[oft:OF_TYPE]-(tt:TumorType) " +
            "RETURN s, ot,t, oft, tt SKIP {from} LIMIT {to}")
    Collection<Sample> findHumanSamplesFromTo(@Param("from") int from, @Param("to") int to);

    @Query("MATCH (mod:ModelCreation)-[ii:IMPLANTED_IN]-(s:Sample) WHERE mod.sourcePdxId = {modelId} AND mod.dataSource = {ds} RETURN s")
    Sample findHumanSampleByModelIdAndDS(@Param("modelId") String modelId, @Param("ds") String ds);


    @Query("MATCH (mod:ModelCreation)--(s:Sample)--(ps:PatientSnapshot) " +
            "WHERE mod.sourcePdxId = {modelId} " +
            "AND mod.dataSource = {ds} " +
            "WITH s " +
            "OPTIONAL MATCH (s)-[cb:CHARACTERIZED_BY]-(mc:MolecularCharacterization) " +
            "RETURN s, cb, mc"
    )
    Sample findHumanSampleWithMolcharByModelIdAndDataSource(@Param("modelId") String modelId, @Param("ds") String ds);


    @Query("MATCH (mod:ModelCreation)-[ii:IMPLANTED_IN]-(s:Sample) " +
            "WHERE mod.sourcePdxId = {sourcePdxId} " +
            "AND toLower(mod.dataSource) = toLower({dataSource}) " +
            "WITH s " +
            "OPTIONAL MATCH (s)-[o:ORIGIN_TISSUE]-(t:Tissue) " +
            "OPTIONAL MATCH (s)-[ssr:SAMPLE_SITE]-(ss:Tissue) " +
            "OPTIONAL MATCH (s)-[ot:OF_TYPE]-(tt:TumorType) " +
            "OPTIONAL MATCH (s)-[mapped:MAPPED_TO]-(oterm:OntologyTerm) " +
            "RETURN s, o, t, ssr, ss, ot, tt, mapped, oterm")
    Sample findPatientSampleWithDetailsByDataSourceAndPdxId(@Param("dataSource") String dataSource, @Param("sourcePdxId") String sourcePdxId);


    @Query("MATCH (s:Sample)-[cbr:CHARACTERIZED_BY]-(mc:MolecularCharacterization) " +
            "WHERE id(mc)= {id} " +
            "RETURN s")
    Sample findSampleByMolcharId(@Param("id") Long id);

}
