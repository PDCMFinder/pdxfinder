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
    @Query("MATCH (st:OntologyTerm)<-[*]-(term:OntologyTerm)-[mapp:MAPPED_TO]-(s:Sample)-[i:IMPLANTED_IN]-(mod:ModelCreation) " +
            "MATCH (s:Sample)-[o:ORIGIN_TISSUE]-(t:Tissue) " +
            "MATCH (s:Sample)-[ot:OF_TYPE]-(tt:TumorType) " +
            "WHERE st.label = {query} " +
            "OR term.label = {query} " +
            "AND (s.dataSource IN {dataSource} OR {dataSource}=[])  " +
            "AND (tt.name IN {tumorType} OR {tumorType}=[])  " +
            "RETURN mod, s,i,o,t,ot, tt")
    Collection<ModelCreation> findByOntology(@Param("query") String query, @Param("markers") String[] markers,
                                             @Param("dataSource") String[] dataSource, @Param("tumorType") String[] tumorType);

    //Ontology powered search: molchar on specimen // BROKEN
    @Query("MATCH (st:OntologyTerm)<-[*]-(term:OntologyTerm)-[mapp:MAPPED_TO]-(s:Sample)-[i:IMPLANTED_IN]-(mod:ModelCreation) " +
            "MATCH (s:Sample)-[o:ORIGIN_TISSUE]-(t:Tissue) " +
            "MATCH (s:Sample)-[cb:CHARACTERIZED_BY]-(mc:MolecularCharacterization)-[aw:ASSOCIATED_WITH]-(ma:MarkerAssociation)-[mar:MARKER]-(m:Marker) " +
            "MATCH (s:Sample)-[ot:OF_TYPE]-(tt:TumorType) " +
            "WHERE st.label = {query} " +
            "OR term.label = {query} " +
            "AND (m.name IN {markers} OR {markers}=[])  " +
            "AND (s.dataSource IN {dataSource} OR {dataSource}=[])  " +
            "AND (tt.name IN {tumorType} OR {tumorType}=[])  " +
            "RETURN mod, s,i,o,t,ot, tt, mc, ma, m, mar, cb, aw")
    Collection<ModelCreation> findByOntology2(@Param("query") String query, @Param("markers") String[] markers,
                                              @Param("dataSource") String[] dataSource, @Param("tumorType") String[] tumorType);

    @Query("MATCH (n:ModelCreation) RETURN n")
    Collection<ModelCreation> getAllModels();



}
