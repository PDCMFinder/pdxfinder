package org.pdxfinder.repositories;


import org.pdxfinder.dao.MolecularCharacterization;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

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
            "MATCH (samp)--(molch:MolecularCharacterization)--(pl:Platform) " +
            "RETURN distinct molch  ")
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
}
