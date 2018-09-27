package org.pdxfinder.repositories;

import org.pdxfinder.dao.Group;
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

    @Query("MATCH (p:Patient)--(g:Group) WHERE p.externalId = {externalId} AND id(g) = {g} RETURN p")
    Patient findByExternalIdAndGroup(@Param("externalId") String externalId, @Param("g") Group g);

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
            "WHERE mod.sourcePdxId = {modelId} " +
            "AND toLower(mod.dataSource) = toLower({dataSource}) "+
            "WITH s " +
            "MATCH (s)-[sf:SAMPLED_FROM]-(ps:PatientSnapshot)-[pt:COLLECTION_EVENT]-(p:Patient)-[ext:GROUP]-(extdsos:Group) " +

            "RETURN s, ps, p, sf, pt, ext, extdsos")
    Patient findByDataSourceAndModelId(@Param("dataSource") String dataSource, @Param("modelId") String modelId);



    @Query("MATCH(pat:Patient)-[patRel:COLLECTION_EVENT]-(ps:PatientSnapshot)-[sfrm:SAMPLED_FROM]-(psamp:Sample)-[char:CHARACTERIZED_BY]-(molch:MolecularCharacterization)-[assoc:ASSOCIATED_WITH]->(mAss:MarkerAssociation)-[aw:MARKER]-(m:Marker) " +
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
            "RETURN DISTINCT mod.sourcePdxId ORDER BY mod.sourcePdxId")
    List<String> getModelsOriginatedFromSamePatientByDataSourceAndModelId(@Param("dataSource") String dataSource,
                                                                          @Param("modelId") String modelId);


    @Query("MATCH (p:Patient)--(g:Group) WHERE p.externalId = {externalId} AND id(g) = {g} " +
            "WITH p " +
            "OPTIONAL MATCH (p)-[ce:COLLECTION_EVENT]-(ps:PatientSnapshot) " +
            "RETURN p, ce, ps")
    Patient findByExternalIdAndGroupWithSnapshots(@Param("externalId") String externalId, @Param("g") Group g);



    @Query("MATCH (mod:ModelCreation)-[ii:IMPLANTED_IN]-(s:Sample) " +
            "            WHERE mod.sourcePdxId = {modelId} " +
            "            AND toLower(mod.dataSource) = toLower({dataSource}) "+
            "            WITH s " +
            "            MATCH (s)-[sf:SAMPLED_FROM]-(pst:PatientSnapshot)--(pat:Patient) " +
            "            WITH pat " +
            "            MATCH (pat)-[cev:COLLECTION_EVENT]-(ps:PatientSnapshot)-[sfrm:SAMPLED_FROM]-(hs:Sample)-[ss:SAMPLE_SITE]-(tiss:Tissue) " +
            "            WITH pat, cev, ps, sfrm, hs, ss, tiss "+
            "            MATCH (hs)-[mto:MAPPED_TO]-(oterm:OntologyTerm)"+

            "            OPTIONAL MATCH (ps)-[st:SUMMARY_OF_TREATMENT]-(ts:TreatmentSummary)-[tpr:TREATMENT_PROTOCOL]-(tp:TreatmentProtocol)-[tcr:TREATMENT_COMPONENT]-(tc:TreatmentComponent)-[drr:DRUG]-(dr:Drug) " +
            "            OPTIONAL MATCH (tp)-[rsp:RESPONSE]-(resp:Response)" +
            "            OPTIONAL MATCH (tp)-[cur:CURRENT_TREATMENT]-(curt:CurrentTreatment)" +

            "            OPTIONAL MATCH (hs)-[char:CHARACTERIZED_BY]-(mc:MolecularCharacterization)-[aw:ASSOCIATED_WITH]-(ma:MarkerAssociation)-[mk:MARKER]-(gene:Marker) " +
            "            OPTIONAL MATCH (hs)-[ot:OF_TYPE]-(tt:TumorType) " +

            "RETURN  pat,cev,ps,sfrm,hs,ss,tiss,  st,ts,tpr,tp,tcr,tc ,drr,dr,   cur,curt,   rsp,resp,   char,mc,aw,ma,mk,gene,   ot,tt, mto, oterm")
    Patient findByPatientByModelId(@Param("dataSource") String dataSource, @Param("modelId") String modelId);


    @Query("MATCH (samp:Sample)-[im:IMPLANTED_IN]-(mod:ModelCreation) " +
            "WHERE mod.dataSource = {dataSource} " +
            "AND samp.sourceSampleId = {sourceSampleId} " +
            " RETURN mod.sourcePdxId ")
    String getModelIdByDataSourceAndPatientSampleId(@Param("dataSource") String dataSource,
                                                                          @Param("sourceSampleId") String modelId);

}

