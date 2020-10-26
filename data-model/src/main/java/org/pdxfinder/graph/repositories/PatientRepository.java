package org.pdxfinder.graph.repositories;

import org.pdxfinder.graph.dao.Group;
import org.pdxfinder.graph.dao.Patient;
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


    @Query("MATCH (ps:PatientSnapshot)-[patRel:COLLECTION_EVENT]-(p:Patient)--(g:Group) WHERE id(g) = {g} RETURN p, patRel, ps ORDER BY p.externalId")
    List<Patient> findByGroup(@Param("g") Group g);


    @Query("MATCH (g:Group)--(p:Patient)-[patRel:COLLECTION_EVENT]-(ps:PatientSnapshot)-[sf:SAMPLED_FROM]-(s:Sample)-[ii:IMPLANTED_IN]-(mod:ModelCreation) " +
            "WHERE id(g) = {g} " +
            "WITH p, patRel, ps, sf, s, ii, mod " +
            "MATCH (s)-[o:ORIGIN_TISSUE]-(t:Tissue) "+
            "MATCH (s)-[ot:OF_TYPE]-(tt:TumorType) " +
            "MATCH (s)-[ssr:SAMPLE_SITE]-(ss:Tissue) "+
            "RETURN p, patRel, ps, sf, s, ii, mod, o, t, ot, tt, ssr, ss ORDER BY p.externalId")
    List<Patient> findPatientTumorAtCollectionDataByDS(@Param("g") Group g);

    @Query("MATCH (mod:ModelCreation)-[ii:IMPLANTED_IN]-(s:Sample) " +
            "WHERE mod.sourcePdxId = {modelId} " +
            "AND toLower(mod.dataSource) = toLower({dataSource}) "+
            "WITH s " +
            "MATCH (s)-[sf:SAMPLED_FROM]-(ps:PatientSnapshot)-[pt:COLLECTION_EVENT]-(p:Patient)-[ext:GROUP]-(extdsos:Group) " +

            "RETURN s, ps, p, sf, pt, ext, extdsos")
    Patient findByDataSourceAndModelId(@Param("dataSource") String dataSource, @Param("modelId") String modelId);


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
            "WHERE mod.sourcePdxId = {modelId} " +
            "AND toLower(mod.dataSource) = toLower({dataSource}) "+
            "WITH s " +
            "MATCH (s)-[sf:SAMPLED_FROM]-(pst:PatientSnapshot)--(pat:Patient) " +
            "WITH pat " +
            "MATCH (pat)-[cev:COLLECTION_EVENT]-(ps:PatientSnapshot)-[sfrm:SAMPLED_FROM]-(hs:Sample)-[ss:SAMPLE_SITE]-(tiss:Tissue) " +
            "WITH pat, cev, ps, sfrm, hs, ss, tiss "+
            "MATCH (hs)-[mto:MAPPED_TO]-(oterm:OntologyTerm)"+

            "OPTIONAL MATCH (ps)-[st:SUMMARY_OF_TREATMENT]-(ts:TreatmentSummary)-[tpr:TREATMENT_PROTOCOL]-" +
            "    (tp:TreatmentProtocol)-[tcr:TREATMENT_COMPONENT]-(tc:TreatmentComponent)-[treatr:TREATMENT]-" +
            "    (treat:Treatment)-[mpt:MAPPED_TO]-(treatoterm:OntologyTerm) " +
            "OPTIONAL MATCH (tp)-[rsp:RESPONSE]-(resp:Response)" +
            "OPTIONAL MATCH (tp)-[cur:CURRENT_TREATMENT]-(curt:CurrentTreatment)" +

            "OPTIONAL MATCH (hs)-[char:CHARACTERIZED_BY]-(mc:MolecularCharacterization)-[aw:ASSOCIATED_WITH]-(ma:MarkerAssociation)-[mk:MARKER]-(gene:Marker) " +
            "OPTIONAL MATCH (hs)-[ot:OF_TYPE]-(tt:TumorType) " +

            "RETURN  pat, cev, ps, sfrm, hs, ss, tiss, st, ts, tpr, tp, tcr, tc, treatr, treatoterm, cur, curt, rsp, resp, char, mc, aw, ma, mk, gene, ot, tt, mto, oterm, mpt")
    Patient findByPatientByModelId(@Param("dataSource") String dataSource, @Param("modelId") String modelId);


    @Query("MATCH (samp:Sample)-[im:IMPLANTED_IN]-(mod:ModelCreation) " +
            "WHERE mod.dataSource = {dataSource} " +
            "AND samp.sourceSampleId = {sourceSampleId} " +
            " RETURN mod.sourcePdxId ")
    String getModelIdByDataSourceAndPatientSampleId(@Param("dataSource") String dataSource,
                                                                          @Param("sourceSampleId") String modelId);

}

