package org.pdxfinder.services;

/*
 * Created by csaba on 06/03/2018.
 */

import org.pdxfinder.dao.DataProjection;
import org.pdxfinder.repositories.DataProjectionRepository;
import org.pdxfinder.repositories.MarkerAssociationRepository;
import org.pdxfinder.repositories.MarkerRepository;
import org.pdxfinder.repositories.MolecularCharacterizationRepository;
import org.springframework.stereotype.Service;

/**
 *
 * This service is responsible for providing data for the Molecular Characterization Facet
 */
@Service
public class MolCharService {

    private MolecularCharacterizationRepository molecularCharacterizationRepository;
    private MarkerAssociationRepository markerAssociationRepository;
    private MarkerRepository markerRepository;
    private DataProjectionRepository dataProjectionRepository;


    public MolCharService(MolecularCharacterizationRepository molecularCharacterizationRepository, MarkerAssociationRepository markerAssociationRepository,
                          MarkerRepository markerRepository, DataProjectionRepository dataProjectionRepository) {

        this.molecularCharacterizationRepository = molecularCharacterizationRepository;
        this.markerAssociationRepository = markerAssociationRepository;
        this.markerRepository = markerRepository;
        this.dataProjectionRepository = dataProjectionRepository;
    }

    public String getMutatedMarkersAndVariants(){

        DataProjection dp = dataProjectionRepository.findByLabel("MarkerVariant");

        if(dp != null){

            if(dp.getValue() != null){
                return dp.getValue();
            }
        }

        return "";
    }



}
