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

    @Query("MATCH(pat:Patient)--(ps:PatientSnapshot)--(samp:Sample)--(molch:MolecularCharacterization) with pat,ps,samp,molch " +
            "Match (samp)-[imp:IMPLANTED_IN]-(mc:ModelCreation) " +
            "WHERE samp.dataSource = {dataSource} AND mc.sourcePdxId = {modelId} " +
            "RETURN distinct molch")
    List<MolecularCharacterization> findPatientPlatformByModelId(@Param("dataSource") String dataSource, @Param("modelId") String modelId);

}
