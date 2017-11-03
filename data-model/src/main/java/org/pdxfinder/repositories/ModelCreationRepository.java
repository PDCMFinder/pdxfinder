package org.pdxfinder.repositories;

import org.pdxfinder.dao.ModelCreation;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

/**
 * Interface describing operations for adding/finding/deleting PDX strain records
 */
public interface ModelCreationRepository extends Neo4jRepository<ModelCreation, Long> {


    ModelCreation findBySourcePdxId(@Param("modelId") String modelId);


    @Query("MATCH (model:ModelCreation) return count(model) ")
    int countAllModels();


    @Query("MATCH (s:Sample)-[i:IMPLANTED_IN]-(mod:ModelCreation) " +
            "WHERE s.sourceSampleId = {sampleId} " +
            "RETURN mod")
    ModelCreation findBySampleId(@Param("sampleId") String sampleId);
/*
    //Simple search on sample diagnosis
    @Query("MATCH (s:Sample)-[o:ORIGIN_TISSUE]-(t:Tissue) " +
            "MATCH (s:Sample)-[cb:CHARACTERIZED_BY]-(mc:MolecularCharacterization)-[aw:ASSOCIATED_WITH]-(ma:MarkerAssociation)-[mar:MARKER]-(m:Marker) " +
            "MATCH (s:Sample)-[ot:OF_TYPE]-(tt:TumorType) " +
            "MATCH (s:Sample)-[ii:IMPLANTED_IN]-(mod:ModelCreation) "+
            "WHERE (toLower(s.diagnosis) CONTAINS toLower({diag}) OR {diag}='') " +
            "AND (m.name IN {markers} OR {markers}=[]) " +
            "AND (s.dataSource IN {dataSource} OR {dataSource}=[]) " +
            "AND (tt.name IN {tumorType} OR {tumorType}=[]) " +
            "RETURN s,o,t,ot, tt, mc, ma, m, mar, cb, aw, ii, mod")
    Collection<ModelCreation> findByMultipleFilters(@Param("diag") String diag, @Param("markers") String[] markers,
                                             @Param("dataSource") String[] dataSource, @Param("tumorType") String[] tumorType);
*/

    @Query("MATCH (s:Sample)-[ii:IMPLANTED_IN]-(mod:ModelCreation)" +
            "WHERE (toLower(s.diagnosis) CONTAINS toLower({diag}) OR {diag}='') " +
            "WITH mod, s, ii " +
            "OPTIONAL MATCH (s)-[o:ORIGIN_TISSUE]-(t:Tissue) " +
            "OPTIONAL MATCH (s)-[cb:CHARACTERIZED_BY]-(mc:MolecularCharacterization)-[aw:ASSOCIATED_WITH]-(ma:MarkerAssociation)-[mar:MARKER]-(m:Marker) " +
            "OPTIONAL MATCH (s)-[ot:OF_TYPE]-(tt:TumorType) " +
            "WHERE (m.name IN {markers} OR {markers}=[]) " +
            "AND (s.dataSource IN {dataSource} OR {dataSource}=[]) " +
            "AND (tt.name IN {tumorType} OR {tumorType}=[]) " +
            "RETURN s,o,t,ot, tt, mc, ma, m, mar, cb, aw, ii, mod")
    Collection<ModelCreation> findByMultipleFilters(@Param("diag") String diag, @Param("markers") String[] markers,
                                                    @Param("dataSource") String[] dataSource, @Param("tumorType") String[] tumorType);


    //Ontology powered search: molchar on samples
    //TODO: Look up mol char and markers, deal with missing nodes
    @Query("MATCH (term:OntologyTerm)-[mapp:MAPPED_TO]-(humSample:Sample)-[i:IMPLANTED_IN]-(mod:ModelCreation) " +
            "WHERE term.label = {query} " +
            "AND (humSample.dataSource IN {dataSource} OR {dataSource}=[])  " +
            "WITH humSample,i,mod " +

            "OPTIONAL MATCH (humSample)-[o:ORIGIN_TISSUE]-(t:Tissue) " +
            "OPTIONAL MATCH (humSample)-[ot:OF_TYPE]-(tt:TumorType) " +
            "WHERE (tt.name IN {tumorType} OR {tumorType}=[])  " +
            "WITH humSample, i, mod, o, t, ot, tt"+

            "MATCH (humSample)-[cb:CHARACTERIZED_BY]-(mc:MolecularCharacterization)-[aw:ASSOCIATED_WITH]-(ma:MarkerAssociation)-[mar:MARKER]-(m:Marker) "+
            "WHERE (m.name IN {markers} OR {markers}=[])  " +
            "RETURN humSample, cb, mc, aw, ma, mar, m, mod, i,o,t, ot, tt "+
            "UNION "+
            "MATCH (mod)-[io:INSTANCE_OF]-(p:Passage)-(pf:PASSAGED_FROM)-(sp:Specimen)-[sf:SAMPLED_FROM]-(mouseSample:Sample) " +
            "-[cb:CHARACTERIZED_BY]-(mc:MolecularCharacterization)-[aw:ASSOCIATED_WITH]-(ma:MarkerAssociation)-[mar:MARKER]-(m:Marker) "+
            "WHERE (m.name IN {markers} OR {markers}=[])  " +
            "RETURN mod, humSample,i,o,t,ot, tt, m")
    Collection<ModelCreation> findByOntology(@Param("query") String query, @Param("markers") String[] markers,
                                             @Param("dataSource") String[] dataSource, @Param("tumorType") String[] tumorType);

    //Ontology powered search: returns less data to improve performance
    @Query("MATCH (term:OntologyTerm)-[mapp:MAPPED_TO]-(humSample:Sample)-[i:IMPLANTED_IN]-(mod:ModelCreation)\n" +
            "        WHERE term.label = {query} " +
            "        AND (humSample.dataSource IN {dataSource} OR []=[]) " +
            "        WITH humSample,i,mod " +

            "        OPTIONAL MATCH (humSample)-[o:ORIGIN_TISSUE]-(t:Tissue) " +
            "        OPTIONAL MATCH (humSample)-[ot:OF_TYPE]-(tt:TumorType) " +
            "        WHERE (tt.name IN {tumorType} OR []=[])  " +
            "        WITH humSample, i, mod, o, t, ot, tt " +

            "        MATCH (mod)—[msr:MODEL_SAMPLE_RELATION]-(s:Sample)-[cb:CHARACTERIZED_BY]-(mc:MolecularCharacterization)-[aw:ASSOCIATED_WITH]-(ma:MarkerAssociation)-[mar:MARKER]-(m:Marker) " +
            "        WHERE (m.name IN {markers} OR []=[])  " +
            "        RETURN mod, humSample, t, tt")
    Collection<ModelCreation> findByOntology2(@Param("query") String query, @Param("markers") String[] markers,
                                              @Param("dataSource") String[] dataSource, @Param("tumorType") String[] tumorType);


    @Query("MATCH (n:ModelCreation) RETURN n")
    Collection<ModelCreation> getAllModels();



}
