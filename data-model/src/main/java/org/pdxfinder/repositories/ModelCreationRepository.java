package org.pdxfinder.repositories;

import org.pdxfinder.dao.ModelCreation;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

/**
 * Interface describing operations for adding/finding/deleting PDX strain records
 *
 * @author csaba
 */
public interface ModelCreationRepository extends Neo4jRepository<ModelCreation, Long> {


    ModelCreation findBySourcePdxId(@Param("modelId") String modelId);

    @Query("MATCH (model:ModelCreation) WHERE model.sourcePdxId = {modelId} AND model.dataSource = {dataSource} RETURN model ")
    ModelCreation findBySourcePdxIdAndDataSource(@Param("modelId") String modelId, @Param("dataSource") String dataSource);

    @Query("MATCH (model:ModelCreation) return count(model) ")
    int countAllModels();


    @Query("MATCH (s:Sample)-[i:IMPLANTED_IN]-(mod:ModelCreation) " +
            "WHERE s.sourceSampleId = {sampleId} " +
            "RETURN mod")
    ModelCreation findBySampleId(@Param("sampleId") String sampleId);

    /*
    //This query is being used when a user does not select an existing ontology term from autosuggest, ie: Football cancer
    @Query("MATCH (humSample:Sample)-[i:IMPLANTED_IN]-(mod:ModelCreation)" +
            "WHERE (toLower(humSample.diagnosis) CONTAINS toLower({diag}) OR {diag}='') " +
            "AND (humSample.dataSource IN {dataSource} OR {dataSource}=[] )"+
            "WITH mod, humSample, i " +

            "MATCH (humSample)-[o:ORIGIN_TISSUE]-(t:Tissue) " +
            "MATCH (humSample)-[ot:OF_TYPE]-(tt:TumorType) " +
            "WHERE (tt.name IN {tumorType} OR {tumorType}=[])  " +
            "WITH humSample, i, mod, o, t, ot, tt " +

            "MATCH (mod)—[msr:MODEL_SAMPLE_RELATION]-(s:Sample)-[cb:CHARACTERIZED_BY]-(mc:MolecularCharacterization)-[aw:ASSOCIATED_WITH]-(ma:MarkerAssociation)-[mar:MARKER]-(m:Marker) " +
            "WHERE (m.name IN {markers} OR {markers}=[])  " +
            "RETURN distinct mod, humSample, s, t, tt, i, ot")
    Collection<ModelCreation> findByMultipleFilters(@Param("diag") String diag, @Param("markers") String[] markers,
                                                    @Param("dataSource") String[] dataSource, @Param("tumorType") String[] tumorType);

    */
    //disable filtering on markers
    @Query("MATCH (humSample:Sample)-[i:IMPLANTED_IN]-(mod:ModelCreation)" +
            "WHERE (toLower(humSample.diagnosis) CONTAINS toLower({diag}) OR {diag}='') " +
            "AND (humSample.dataSource IN {dataSource} OR {dataSource}=[] )"+
            "WITH mod, humSample, i " +

            "MATCH (humSample)-[o:ORIGIN_TISSUE]-(t:Tissue) " +
            "MATCH (humSample)-[ot:OF_TYPE]-(tt:TumorType) " +
            "WHERE (tt.name IN {tumorType} OR {tumorType}=[])  " +
            "OPTIONAL MATCH (humSample)-[mto:MAPPED_TO]-(oterm:OntologyTerm) " +
            "RETURN humSample, i, mod, o, t, ot, tt, mto, oterm ")
    Collection<ModelCreation> findByMultipleFilters(@Param("diag") String diag, @Param("markers") String[] markers,
                                                    @Param("dataSource") String[] dataSource, @Param("tumorType") String[] tumorType);


    /*
    //Ontology powered search: returns less data to improve performance
    @Query("MATCH (term:OntologyTerm)<-[*]-(child:OntologyTerm)-[mapp:MAPPED_TO]-(humSample:Sample)-[i:IMPLANTED_IN]-(mod:ModelCreation) " +
            "        WHERE term.label = {query} " +
            "        AND (humSample.dataSource IN {dataSource} OR {dataSource}=[]) " +
            "        WITH humSample,i,mod " +

            "        MATCH (humSample)-[o:ORIGIN_TISSUE]-(t:Tissue) " +
            "        MATCH (humSample)-[ot:OF_TYPE]-(tt:TumorType) " +
            "        WHERE (tt.name IN {tumorType} OR {tumorType}=[])  " +
            "        WITH humSample, i, mod, o, t, ot, tt " +

            "        MATCH (mod)—[msr:MODEL_SAMPLE_RELATION]-(s:Sample)-[cb:CHARACTERIZED_BY]-(mc:MolecularCharacterization)-[aw:ASSOCIATED_WITH]-(ma:MarkerAssociation)-[mar:MARKER]-(m:Marker) " +
            "        WHERE (m.name IN {markers} OR []=[])  " +
            "        RETURN distinct mod, humSample, t, tt, i, ot")
    Collection<ModelCreation> findByOntology(@Param("query") String query, @Param("markers") String[] markers,
                                              @Param("dataSource") String[] dataSource, @Param("tumorType") String[] tumorType);

*/


    //Ontology powered search: returns less data to improve performance
    @Query("MATCH (term:OntologyTerm)<-[*0..]-(child:OntologyTerm)-[mapp:MAPPED_TO]-(humSample:Sample)-[i:IMPLANTED_IN]-(mod:ModelCreation) " +
            "        WHERE term.label = {query} " +
            "        AND (humSample.dataSource IN {dataSource} OR {dataSource}=[]) " +
            "        WITH humSample,i,mod,mapp,child " +

            "        MATCH (humSample)-[o:ORIGIN_TISSUE]-(t:Tissue) " +
            "        MATCH (humSample)-[ot:OF_TYPE]-(tt:TumorType) " +
            "        WHERE (tt.name IN {tumorType} OR {tumorType}=[])  " +
            "        return humSample, i, mod, o, t, ot, tt,mapp,child ")
    Collection<ModelCreation> findByOntology(@Param("query") String query, @Param("markers") String[] markers,
                                             @Param("dataSource") String[] dataSource, @Param("tumorType") String[] tumorType);

    @Query("MATCH (n:ModelCreation) RETURN n")
    Collection<ModelCreation> getAllModels();



}
