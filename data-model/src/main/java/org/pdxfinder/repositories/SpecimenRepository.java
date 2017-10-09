package org.pdxfinder.repositories;

import org.pdxfinder.dao.Specimen;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

/**
 * Created by jmason on 08/06/2017.
 */
public interface SpecimenRepository extends Neo4jRepository<Specimen, Long> {

    Specimen findByExternalId(@Param("externalId") String externalId);


    @Query("MATCH (s:Sample)--(mod:ModelCreation)--(pass:PdxPassage)--(spec:Specimen) " +
            "WHERE s.dataSource = {dataSource} " +
            "AND mod.sourcePdxId = {modelId} " +
            "AND pass.passage = {passage} " +
            "AND spec.externalId = {specimenId} " +
            "RETURN spec")
    Specimen findByModelIdAndDataSourceAndSpecimenIdAndPassage(
            @Param("modelId") String modelId,
            @Param("dataSource") String dataSource,
            @Param("specimenId") String specimenId,
            @Param("passage") int passage);



    @Query("MATCH (psamp:Sample)--(mod:ModelCreation)-[io:INSTANCE_OF]-(pdxPass:PdxPassage)-[passfrm:PASSAGED_FROM]-(spec:Specimen)-[sfrm:SAMPLED_FROM]-(msamp:Sample) " +
            "            -[char:CHARACTERIZED_BY]-(molchar:MolecularCharacterization)-[assoc:ASSOCIATED_WITH]->(mAss:MarkerAssociation)-[aw:MARKER]-(m:Marker) " +
            "            WITH psamp, mod, spec, passfrm,pdxPass, sfrm,msamp, char,molchar, assoc,mAss, aw,m " +
            "            MATCH (molchar)-[pl:PLATFORM_USED]-(tech:Platform) " +

            "            WHERE  psamp.dataSource = {dataSource}  " +
            "            AND    mod.sourcePdxId = {modelId}  " +

            "            AND ( toLower(spec.externalId) CONTAINS toLower({search})" +
            "            OR toLower(m.symbol) CONTAINS toLower({search})" +
            "            OR toLower(tech.name) CONTAINS toLower({search})" +
            "            OR any( property in keys(mAss) where toLower(mAss[property]) CONTAINS toLower({search}) ) ) " +

            "            RETURN pdxPass, passfrm, spec, sfrm,msamp, char,molchar, assoc,mAss, aw,m,pl,tech SKIP {skip} LIMIT {lim} ")
    Set<Specimen> findVariationDataBySourcePdxId(@Param("dataSource") String dataSource,
                                                 @Param("modelId") String modelId,
                                                 @Param("search") String search,
                                                 @Param("skip") int skip,
                                                 @Param("lim") int lim);


    @Query("MATCH (mod:ModelCreation) where mod.sourcePdxId = {modelId} with mod" +
            "  optional match (mod)-[io:INSTANCE_OF]-(pdxPass:PdxPassage)-[passfrm:PASSAGED_FROM]-(spec:Specimen)-[sfr:SAMPLED_FROM]->(sample:Sample)<-[char:CHARACTERIZED_BY]-(molchar:MolecularCharacterization)-[assoc:ASSOCIATED_WITH]-(mAss:MarkerAssociation)-[aw:MARKER]-(m:Marker) " +
            "  return spec")
    List<Specimen> findVariationDataBySourcePdxId2(@Param("dataSource") String dataSource, @Param("modelId") String modelId);

}
