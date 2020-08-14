package org.pdxfinder.graph.repositories;

import org.pdxfinder.graph.dao.ModelCreation;
import org.pdxfinder.graph.dao.MolecularCharacterization;
import org.pdxfinder.graph.dao.Specimen;
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

    @Query("MATCH (sp:Specimen) WHERE sp.externalId = {externalId} " +
            "OPTIONAL MATCH (sp)-[sfrm:SAMPLED_FROM]-(msamp:Sample)-[cb:CHARACTERIZED_BY]-(mc:MolecularCharacterization) " +
            "RETURN sp, sfrm, msamp, cb, mc")
    Specimen findByExternalIdWithMolecularCharacterizations(@Param("externalId") String externalId);

    @Query("MATCH (mod:ModelCreation)--(spec:Specimen) " +
            "WHERE mod.dataSource = {dataSource} " +
            "AND mod.sourcePdxId = {modelId} " +
            "AND spec.passage = {passage} " +
            "AND spec.externalId = {specimenId} " +
            "WITH spec " +
            "OPTIONAL MATCH (spec)-[sfr:SAMPLED_FROM]-(s:Sample) " +

            "RETURN spec, sfr, s")
    Specimen findByModelIdAndDataSourceAndSpecimenIdAndPassage(
            @Param("modelId") String modelId,
            @Param("dataSource") String dataSource,
            @Param("specimenId") String specimenId,
            @Param("passage") String passage);


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

    @Query("MATCH (mod:ModelCreation)--(sp:Specimen)--(hs:HostStrain) " +
            "WHERE id(mod) = {model} " +
            "AND sp.passage = {passage} " +
            "AND hs.symbol = {symbol}" +
            "WITH sp " +
            "OPTIONAL MATCH (sp)-[sfr:SAMPLED_FROM]-(s:Sample)-[cbr:CHARACTERIZED_BY]-(mc:MolecularCharacterization)-[pur:PLATFORM_USED]-(pl:Platform) " +
            "RETURN sp, sfr, s, cbr, mc, pur, pl")
    Specimen findByModelAndPassageAndNomenClature(@Param("model") ModelCreation modelCreation, @Param("passage") String passage, @Param("symbol") String nomenclature);

    @Query("MATCH (sp:Specimen)--(s:Sample)--(mc:MolecularCharacterization) " +
            "WHERE id(mc) = {mc} " +
            "RETURN sp")
    Specimen findByMolChar(@Param("mc")MolecularCharacterization mc);

    @Query("MATCH (mod:ModelCreation)--(sp:Specimen) " +
            "WHERE id(mod) = {model} " +
            "WITH sp " +
            "OPTIONAL MATCH (sp)-[sfr:SAMPLED_FROM]-(s:Sample)-[cbr:CHARACTERIZED_BY]-(mc:MolecularCharacterization)-[pur:PLATFORM_USED]-(pl:Platform) " +
            "RETURN sp, sfr, s, cbr, mc, pur, pl")
    List<Specimen> findAllWithMolcharDataByModel(@Param("model") ModelCreation model);
}
