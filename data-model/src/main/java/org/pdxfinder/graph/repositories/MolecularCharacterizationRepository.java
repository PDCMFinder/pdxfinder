package org.pdxfinder.graph.repositories;


import org.pdxfinder.graph.dao.MolecularCharacterization;
import org.pdxfinder.graph.dao.Sample;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Interface to the molecular characterization experiments done
 */
@Repository
public interface MolecularCharacterizationRepository extends PagingAndSortingRepository<MolecularCharacterization, Long> {

    MolecularCharacterization findByTechnology(@Param("technology") String technology);

    @Query("MATCH (ps:PatientSnapshot)--(samp:Sample)--(mod:ModelCreation)  " +
            "WHERE mod.sourcePdxId = {modelId} " +
            "AND mod.dataSource = {dataSource} " +
            "WITH samp " +
            "MATCH (samp)--(molch:MolecularCharacterization)-[plr:PLATFORM_USED]-(pl:Platform) " +
            "RETURN distinct molch, plr, pl  ")
    List<MolecularCharacterization> findPatientPlatformByModelId(@Param("dataSource") String dataSource, @Param("modelId") String modelId);


    @Query("MATCH (molch:MolecularCharacterization) " +
            "WHERE molch.type = {type} " +
            "WITH molch " +
            "MATCH (molch)-[awr:ASSOCIATED_WITH]-(mAss:MarkerAssociation) " +
            "WHERE exists(mAss.aminoAcidChange) " +
            "WITH molch, awr, mAss " +
            "MATCH (mAss)-[mr:MARKER]-(m:Marker) " +
            "RETURN distinct molch, awr, mAss, mr, m")
    Collection<MolecularCharacterization> getAllDistinctByType(@Param("type") String type);

    @Query("MATCH (molch:MolecularCharacterization) " +
            "WHERE molch.type = {type} " +
            "OPTIONAL MATCH (molch)-[plr:PLATFORM_USED]-(pl:Platform) " +
            "RETURN molch, plr, pl")
    Collection<MolecularCharacterization> findAllByType(@Param("type") String type);

    @Query("MATCH (pl:Platform)-[plr:PLATFORM_USED]-(mc:MolecularCharacterization)--(s:Sample) " +
            "WHERE id(s) = {sample} " +
            "RETURN mc, plr, pl")
    Collection<MolecularCharacterization> findAllBySample(@Param("sample") Sample sample);

    @Query("MATCH (pl:Platform)-[plr:PLATFORM_USED]-(mc:MolecularCharacterization) " +
            "WHERE ID(mc) = {id} " +
            "WITH pl, plr, mc " +
            "OPTIONAL MATCH (mc)-[awr:ASSOCIATED_WITH]-(ma:MarkerAssociation) " +
            "RETURN pl, plr, mc, awr, ma")
    MolecularCharacterization getMolecularDataById(@Param("id") Long id);

    @Query("MATCH (mc:MolecularCharacterization) RETURN ID(mc)")
    List<Long> getAllMolCharIDs();

    @Query("MATCH (mc:MolecularCharacterization)-[cbr:CHARACTERIZED_BY]-(s:Sample)-[msr:MODEL_SAMPLE_RELATION]-(mod:ModelCreation) " +
            "WHERE mod.dataSource = {ds} " +
            "RETURN mc")
    List<MolecularCharacterization> findAllByDataSource(@Param("ds") String dataSource);

    @Query("MATCH (mc:MolecularCharacterization)-[awr:ASSOCIATED_WITH]-(mAss:MarkerAssociation) " +
            "WHERE ID(mc) = {id} " +
            "RETURN sum(mAss.dataPoints) ")
    int findAssociationsNumberById(@Param("id") MolecularCharacterization mc);

    @Query("MATCH (mc:MolecularCharacterization) WHERE id(mc) IN {ids} RETURN mc")
    Set<MolecularCharacterization> findByIds(@Param("ids")Set<Long> ids);

}
