package org.pdxfinder.repositories;

import org.pdxfinder.dao.Specimen;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by jmason on 08/06/2017.
 */
@Repository
public interface SpecimenRepository extends Neo4jRepository<Specimen, Long> {

    Specimen findByExternalId(@Param("externalId") String externalId);


    @Query("MATCH (mod:ModelCreation)--(spec:Specimen) " +
            "WHERE mod.dataSource = {dataSource} " +
            "AND mod.sourcePdxId = {modelId} " +
            "AND spec.passage = {passage} " +
            "AND spec.externalId = {specimenId} " +
            "RETURN spec")
    Specimen findByModelIdAndDataSourceAndSpecimenIdAndPassage(
            @Param("modelId") String modelId,
            @Param("dataSource") String dataSource,
            @Param("specimenId") String specimenId,
            @Param("passage") String passage);

    @Query("MATCH (mod:ModelCreation)--(spec:Specimen) " +
            "WHERE mod.dataSource = {dataSource} " +
            "AND mod.sourcePdxId = {modelId} " +
            "AND spec.passage = {passage} " +
            "RETURN spec")
    List<Specimen> findByModelIdAndDataSourceAndAndPassage(
            @Param("modelId") String modelId,
            @Param("dataSource") String dataSource,
            @Param("passage") String passage);


    @Query("MATCH (mod:ModelCreation)-[sp:SPECIMENS]-(spec:Specimen)-[sfrm:SAMPLED_FROM]-(msamp:Sample)" +
            "-[char:CHARACTERIZED_BY]-(molchar:MolecularCharacterization)-[assoc:ASSOCIATED_WITH]->(mAss:MarkerAssociation)-[aw:MARKER]-(m:Marker) " +
            "            WITH mod, sp, spec, sfrm,msamp, char,molchar, assoc,mAss, aw,m " +
            "            MATCH (molchar)-[pl:PLATFORM_USED]-(tech:Platform) " +

            "            WHERE  mod.dataSource = {dataSource}  " +
            "            AND    mod.sourcePdxId = {modelId}  " +
            "            AND    (tech.name = {tech}  OR {tech} = '' ) " +
            "            AND    (spec.passage = {passage} OR {passage} = '' )" +

            "            AND ( toLower(spec.externalId) CONTAINS toLower({search})" +
            "            OR toLower(m.symbol) CONTAINS toLower({search})" +
            "            OR toLower(tech.name) CONTAINS toLower({search})" +
            "            OR any( property in keys(mAss) where toLower(mAss[property]) CONTAINS toLower({search}) ) ) " +

            "            RETURN spec, sp, sfrm,msamp, char,molchar, assoc,mAss, aw,m,pl,tech ")
    Page<Specimen> findSpecimenBySourcePdxIdAndPlatform(@Param("dataSource") String dataSource,
                                                        @Param("modelId") String modelId,
                                                        @Param("tech") String tech,
                                                        @Param("passage") String passage,
                                                        @Param("search") String search, Pageable pageable);




    @Query("MATCH (mod:ModelCreation)-[spr:SPECIMENS]-(sp:Specimen) " +
            "WHERE  mod.dataSource = {dataSource}  " +
            "AND    mod.sourcePdxId = {modelId}  " +
            "WITH sp " +
            "OPTIONAL MATCH (sp)-[etr:ENGRAFTMENT_TYPE]-(et:EngraftmentType) " +
            "OPTIONAL MATCH (sp)-[esr:ENGRAFTMENT_SITE]-(es:EngraftmentSite) " +
            "OPTIONAL MATCH (sp)-[emr:ENGRAFTMENT_MATERIAL]-(em:EngraftmentMaterial) " +
            "OPTIONAL MATCH (sp)-[hsr:HOST_STRAIN]-(hs:HostStrain) " +
            "RETURN sp, etr, et, esr, es, emr, em, hsr, hs")
    List<Specimen> findByDataSourceAndModelId(@Param("dataSource") String dataSource,
                                              @Param("modelId") String modelId);

    @Query("MATCH (mod:ModelCreation)-[sp:SPECIMENS]-(spec:Specimen)-[sfrm:SAMPLED_FROM]-(msamp:Sample) " +
            "            -[char:CHARACTERIZED_BY]-(molchar:MolecularCharacterization) " +
            "            WITH mod, spec, sp, sfrm,msamp, char,molchar " +
            "            MATCH (molchar)-[pl:PLATFORM_USED]-(tech:Platform) " +

            "            WHERE  mod.dataSource = {dataSource}  " +
            "            AND    mod.sourcePdxId = {modelId}  " +
            "            AND    (tech.name = {tech}  OR {tech} = '' ) " +

            "            RETURN spec, sfrm,msamp, char,molchar,pl,tech ")
    List<Specimen> findSpecimenBySourcePdxIdAndPlatform2(@Param("dataSource") String dataSource,
                                                        @Param("modelId") String modelId,
                                                        @Param("tech") String tech);



    @Query("MATCH (mod:ModelCreation)--(spec:Specimen)--(msamp:Sample)--(molchar:MolecularCharacterization)-->(mAss:MarkerAssociation)--(m:Marker) " +
            "            WITH mod,spec,msamp,molchar,mAss,m " +
            "            MATCH (molchar)--(tech:Platform) " +

            "            WHERE  mod.dataSource = {dataSource}  " +
            "            AND    mod.sourcePdxId = {modelId}  " +
            "            AND    (tech.name = {tech}  OR {tech} = '' ) " +
            "            AND    (spec.passage = {passage} OR {passage} = '' )" +

            "            AND ( toLower(spec.externalId) CONTAINS toLower({search})" +
            "            OR toLower(m.symbol) CONTAINS toLower({search})" +
            "            OR toLower(tech.name) CONTAINS toLower({search})" +
            "            OR any( property in keys(mAss) where toLower(mAss[property]) CONTAINS toLower({search}) ) ) " +
            "            RETURN count(*) ")
    Integer countBySearchParameterAndPlatform(@Param("dataSource") String dataSource,
                                              @Param("modelId") String modelId,
                                              @Param("tech") String tech,
                                              @Param("passage") String passage,
                                              @Param("search") String search);



    @Query("MATCH (mod:ModelCreation)--(spec:Specimen)--(msamp:Sample)--(molchar:MolecularCharacterization)-->(mAss:MarkerAssociation)--(m:Marker) " +
            "            WITH mod,spec,msamp,molchar,mAss,m " +
            "            MATCH (molchar)--(tech:Platform) " +

            "            WHERE  mod.dataSource = {dataSource}  " +
            "            AND    mod.sourcePdxId = {modelId}  " +
            "            AND    (tech.name = {tech}  OR {tech} = '' ) " +
            "            AND    (spec.passage = {passage} OR {passage} = '' )" +

            "            RETURN count(*) ")
    Integer countByPlatform(@Param("dataSource") String dataSource,
                                              @Param("modelId") String modelId,
                                              @Param("tech") String tech,
                                              @Param("passage") String passage);


    @Query("MATCH (mod:ModelCreation)--(sp:Specimen) WHERE mod.sourcePdxId = {modelId} AND mod.dataSource = {dataSource} RETURN sp")
    List<Specimen> findByModelIdAndDataSource(@Param("modelId") String modelId, @Param("dataSource") String dataSource);

}
