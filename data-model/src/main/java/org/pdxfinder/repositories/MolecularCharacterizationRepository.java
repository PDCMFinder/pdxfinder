package org.pdxfinder.repositories;


import org.pdxfinder.dao.MolecularCharacterization;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Interface to the molecular characterization experiments done
 */
public interface MolecularCharacterizationRepository extends PagingAndSortingRepository<MolecularCharacterization, Long> {

    MolecularCharacterization findByTechnology(@Param("technology") String technology);

    @Query("MATCH (ps:PatientSnapshot)--(samp:Sample)--(mod:ModelCreation)  " +
            "WHERE mod.sourcePdxId = {modelId} " +
            "AND mod.dataSource = {dataSource} " +
            "WITH samp " +
            "MATCH (samp)--(molch:MolecularCharacterization)--(pl:Platform) " +
            "RETURN distinct molch  ")
    List<MolecularCharacterization> findPatientPlatformByModelId(@Param("dataSource") String dataSource, @Param("modelId") String modelId);

}
