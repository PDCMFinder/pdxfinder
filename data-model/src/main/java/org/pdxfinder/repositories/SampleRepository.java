package org.pdxfinder.repositories;


import org.pdxfinder.dao.Sample;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Set;

/**
 * Repository for managing creating, finding, and deleting sample objects
 */
public interface SampleRepository extends PagingAndSortingRepository<Sample, Long> {

    @Query("MATCH (s:Sample) WHERE s.sourceSampleId = {sourceSampleId} AND s.dataSource = {dataSource} return s")
    Sample findBySourceSampleIdAndDataSource(@Param("sourceSampleId") String sourceSampleId, @Param("dataSource") String dataSource) ;

    Set<Sample> findByDataSource(String dataSource);

    @Query("MATCH (s:Sample)-[o:ORIGIN_TISSUE]-(t:Tissue) where s.diagnosis contains {diag} return s,o,t order by s.diagnosis limit 30")
    Collection<Sample> findByDiagnosisContains(@Param("diag") String diag);

    @Query("MATCH (s:Sample)-[o:ORIGIN_TISSUE]-(t:Tissue) " +
            "MATCH (s:Sample)--(:MolecularCharacterization)--(:MarkerAssociation)--(m:Marker) " +
            "WHERE toLower(s.diagnosis) CONTAINS toLower({diag}) " +
            "AND m.name IN {markers} return s,o,t")
    Collection<Sample> findByDiagnosisContainsAndHaveMarkers(@Param("diag") String diag, @Param("markers") String[] markers);


    @Query("MATCH (s:Sample)-[o:ORIGIN_TISSUE]-(t:Tissue) " +
            "MATCH (s:Sample)-[cb:CHARACTERIZED_BY]-(mc:MolecularCharacterization)-[aw:ASSOCIATED_WITH]-(ma:MarkerAssociation)-[mar:MARKER]-(m:Marker) " +
            "MATCH (s:Sample)-[ot:OF_TYPE]-(tt:TumorType) " +
            "MATCH (s:Sample)-[ii:IMPLANTED_IN]-(mod:ModelCreation) "+
            "WHERE (toLower(s.diagnosis) CONTAINS toLower({diag}) OR {diag}='') " +
            "AND (m.name IN {markers} OR {markers}=[]) " +
            "AND (s.dataSource IN {dataSource} OR {dataSource}=[]) " +
            "AND (tt.name IN {tumorType} OR {tumorType}=[]) " +
            "RETURN s,o,t,ot, tt, mc, ma, m, mar, cb, aw, ii, mod")
    Collection<Sample> findByMultipleFilters(@Param("diag") String diag, @Param("markers") String[] markers,
                                             @Param("dataSource") String[] dataSource, @Param("tumorType") String[] tumorType);



    @Query("MATCH (mod:ModelCreation)-[ii:IMPLANTED_IN]-(s:Sample) " +
            "WHERE mod.sourcePdxId = {sourcePdxId} " +
            "WITH s, mod, ii " +
            "OPTIONAL MATCH (s)-[o:ORIGIN_TISSUE]-(t:Tissue) " +
            "OPTIONAL MATCH (s)-[cb:CHARACTERIZED_BY]-(mc:MolecularCharacterization)-[aw:ASSOCIATED_WITH]-(ma:MarkerAssociation)-[mar:MARKER]-(m:Marker) " +
            "OPTIONAL MATCH (s)-[ot:OF_TYPE]-(tt:TumorType) " +
            "RETURN mod,ii,s,o,t,ot, tt, mc, ma, m, mar, cb, aw")
    Sample findBySourcePdxId(@Param("sourcePdxId") String sourcePdxId);


    @Query("MATCH (mod:ModelCreation)-[ii:IMPLANTED_IN]-(s:Sample) " +
            "WHERE mod.sourcePdxId = {sourcePdxId} " +
            "AND toLower(s.dataSource) = toLower({dataSource}) " +
            "WITH s, mod, ii " +
            "OPTIONAL MATCH (s)-[o:ORIGIN_TISSUE]-(t:Tissue) " +
            "OPTIONAL MATCH (s)-[cb:CHARACTERIZED_BY]-(mc:MolecularCharacterization)-[aw:ASSOCIATED_WITH]-(ma:MarkerAssociation)-[mar:MARKER]-(m:Marker) " +
            "OPTIONAL MATCH (s)-[ot:OF_TYPE]-(tt:TumorType) " +
            "OPTIONAL MATCH (s)-[mapped:MAPPED_TO]-(oterm:OntologyTerm) " +
            "RETURN mod,ii,s,o,t,ot, tt, mc, ma, m, mar, cb, aw, mapped, oterm")
    Sample findByDataSourceAndPdxId(@Param("dataSource") String dataSource, @Param("sourcePdxId") String sourcePdxId);


    @Query("MATCH (p:Patient--(ps:PatientSnapshot)--(s:Sample) " +
            "WHERE p.externalId = {patientId} " +
            "AND p.dataSource = {dataSource} " +
            "AND s.sourceSampleId = {sampleId} " +
            "RETURN s")
    Sample findHumanSampleByPatientIdAndDataSourceAndSampleId(@Param("patientId") String patientId,
                                                              @Param("dataSource") String dataSource,
                                                              @Param("sampleId") String sampleId);

    @Query("MATCH (ps:PatientSnapshot)--(s:Sample) WHERE s.dataSource = {dataSource} AND s.sourceSampleId = {sampleId} RETURN s")
    Sample findHumanSampleBySampleIdAndDataSource(@Param("sampleId") String sampleId, @Param("dataSource") String dataSource);

    @Query("MATCH (sp:Specimen)--(s:Sample) WHERE s.sourceSampleId = {sampleId} AND s.dataSource = {dataSource} RETURN s")
    Sample findMouseSampleBySampleIdAndDataSource(@Param("sampleId") String sampleId, @Param("dataSource") String dataSource);

    @Query("MATCH (ps:PatientSnapshot)--(s:Sample) RETURN count(s)")
    int findHumanSamplesNumber();

    @Query("MATCH (ps:PatientSnapshot)--(s:Sample) " +
            "WITH s " +
            "OPTIONAL MATCH (s)-[ot:ORIGIN_TISSUE]-(t:Tissue) " +
            "OPTIONAL MATCH (s)-[oft:OF_TYPE]-(tt:TumorType) " +
            "RETURN s, ot,t, oft, tt SKIP {from} LIMIT {to}")
    Collection<Sample> findHumanSamplesFromTo(@Param("from") int from, @Param("to") int to);

    @Query("MATCH (ps:PatientSnapshot)-[sf:SAMPLED_FROM]-(s:Sample) WHERE NOT (s)-[:MAPPED_TO]-() " +
            "WITH s " +
            "OPTIONAL MATCH (s)-[ot:ORIGIN_TISSUE]-(ti:Tissue) " +
            "OPTIONAL MATCH  (s)-[oft:OF_TYPE]-(t:TumorType) " +
            "RETURN DISTINCT  s, ti, t, ot, oft")
    Collection<Sample> findSamplesWithoutOntologyMapping();
}
