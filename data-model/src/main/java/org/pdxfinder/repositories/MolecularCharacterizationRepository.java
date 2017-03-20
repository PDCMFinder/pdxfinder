package org.pdxfinder.repositories;

import org.pdxfinder.dao.MolecularCharacterization;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

/**
 * Interface to the molecular characterization experiments done
 */
public interface MolecularCharacterizationRepository extends PagingAndSortingRepository<MolecularCharacterization, Long> {

    MolecularCharacterization findByTechnology(@Param("technology") String technology);

}
