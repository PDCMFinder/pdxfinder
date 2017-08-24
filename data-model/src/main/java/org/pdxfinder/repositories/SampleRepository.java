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

    @Query("MATCH (s:Sample) WHERE s.sourceSampleId = {sourceSampleId} return s")
    Sample findBySourceSampleId(@Param("sourceSampleId") String sourceSampleId);

    Set<Sample> findByDataSource(String dataSource);

    Set<Sample> findByExternalDataSourceAbbreviation(String abbreviation);

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
            "RETURN mod,ii,s,o,t,ot, tt, mc, ma, m, mar, cb, aw")
    Sample findByDataSourceAndPdxId(@Param("dataSource") String dataSource, @Param("sourcePdxId") String sourcePdxId);

}
