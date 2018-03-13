package org.pdxfinder.repositories;

import org.pdxfinder.dao.Patient;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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
            "MATCH (s:Sample)-[sf:SAMPLED_FROM]-(ps:PatientSnapshot)-[pt:PATIENT]-(p:Patient)-[ext:EXTERNAL_DATASOURCE]-(extdsos:ExternalDataSource) " +
            "WHERE mod.sourcePdxId = {modelId} " +
            "AND toLower(s.dataSource) = toLower({dataSource}) "+
            "RETURN mod, ii, s, ps, p, sf, pt, ext, extdsos")
    Patient findByDataSourceAndModelId(@Param("dataSource") String dataSource, @Param("modelId") String modelId);



    @Query("MATCH(pat:Patient)-[patRel:PATIENT]-(ps:PatientSnapshot)-[sfrm:SAMPLED_FROM]-(psamp:Sample)-[char:CHARACTERIZED_BY]-(molch:MolecularCharacterization)-[assoc:ASSOCIATED_WITH]->(mAss:MarkerAssociation)-[aw:MARKER]-(m:Marker) " +
            "WITH pat,patRel,ps,sfrm,psamp,char,molch,mAss,m " +
            "Match (psamp)-[imp:IMPLANTED_IN]-(mc:ModelCreation) " +
            "            WHERE  psamp.dataSource = {dataSource}  " +
            "            AND    mc.sourcePdxId = {modelId}  " +
            "            AND    (mc.technology = {tech}  OR {tech} = '' ) " +


            "            OR toLower(m.symbol) CONTAINS toLower({search})" +
            "            OR toLower(mc.technology) CONTAINS toLower({search})" +
            "            OR any( property in keys(mAss) where toLower(mAss[property]) CONTAINS toLower({search}) )  " +

            "            RETURN pat,patRel,ps,sfrm,psamp,char,molch,mAss,m SKIP {skip} LIMIT {lim} ")
    Set<Patient> findSpecimenBySourcePdxIdAndPlatform(@Param("dataSource") String dataSource,
                                                       @Param("modelId") String modelId,
                                                       @Param("tech") String tech,
                                                       @Param("search") String search,
                                                       @Param("skip") int skip,
                                                       @Param("lim") int lim);





    @Query("MATCH(pat:Patient)-[patRel:PATIENT]-(ps:PatientSnapshot)-[sfrm:SAMPLED_FROM]-(psamp:Sample)-[char:CHARACTERIZED_BY]-(molch:MolecularCharacterization)-[assoc:ASSOCIATED_WITH]->(mAss:MarkerAssociation)-[aw:MARKER]-(m:Marker) " +
            "WITH pat,patRel,ps,sfrm,psamp,char,molch,mAss,m " +
            "Match (psamp)-[imp:IMPLANTED_IN]-(mc:ModelCreation) " +
            "            WHERE  psamp.dataSource = {dataSource}  " +
            "            AND    mc.sourcePdxId = {modelId}  " +
            "            AND    (mc.technology = {tech}  OR {tech} = '' ) " +

            "            OR toLower(m.symbol) CONTAINS toLower({search}) " +
            "            OR toLower(mc.technology) CONTAINS toLower({search}) " +
            "            OR any( property in keys(mAss) where toLower(mAss[property]) CONTAINS toLower({search}) )  " +
            "            RETURN count(*) ")
    Integer countByBySourcePdxIdAndPlatform(@Param("dataSource") String dataSource,
                                                      @Param("modelId") String modelId,
                                                      @Param("tech") String tech,
                                                      @Param("search") String search);



    @Query("MATCH (p:Patient)--(ps:PatientSnapshot)--(s:Sample)--(mod:ModelCreation) " +
            "WHERE mod.dataSource = {dataSource} " +
            "AND mod.sourcePdxId = {modelId} " +
            "WITH p " +
            "MATCH (p:Patient)--(ps:PatientSnapshot)--(s:Sample)--(mod:ModelCreation) " +
            "WHERE mod.sourcePdxId <> {modelId} " +
            "RETURN mod.sourcePdxId")
    List<String> getModelsOriginatedFromSamePatientByDataSourceAndModelId(@Param("dataSource") String dataSource,
                                                                          @Param("modelId") String modelId);

}
