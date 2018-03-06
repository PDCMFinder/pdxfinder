package org.pdxfinder.services;

/*
 * Created by csaba on 06/03/2018.
 */

import org.neo4j.cypher.internal.frontend.v2_3.ast.functions.Str;
import org.pdxfinder.dao.MolecularCharacterization;
import org.pdxfinder.repositories.MolecularCharacterizationRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * This service is responsible for providing data for the Molecular Characterization Facet
 */
@Service
public class MolCharService {

    private MolecularCharacterizationRepository molecularCharacterizationRepository;

    public MolCharService(MolecularCharacterizationRepository molecularCharacterizationRepository) {
        this.molecularCharacterizationRepository = molecularCharacterizationRepository;
    }


    public Map<String, Set<String>> getMutatedMarkersAndVariants(){


        // "KRAS"=>("V600","V600E","V612")
        Map<String, Set<String>> result = new HashMap<>();

        Collection<MolecularCharacterization> mutatedMolchars = molecularCharacterizationRepository.getAllDistinctByType("MUTATED");

        for(MolecularCharacterization mc:mutatedMolchars){

            //TODO: add variants and markers to results
        }

        return result;
    }



}
