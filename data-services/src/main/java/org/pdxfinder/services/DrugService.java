package org.pdxfinder.services;

import org.pdxfinder.repositories.TreatmentSummaryRepository;
import org.springframework.stereotype.Service;

/*
 * Created by csaba on 29/03/2018.
 */
@Service
public class DrugService {

    private TreatmentSummaryRepository treatmentSummaryRepository;

    public DrugService(TreatmentSummaryRepository treatmentSummaryRepository) {
        this.treatmentSummaryRepository = treatmentSummaryRepository;
    }


    public int getDosingStudiesNumberByDataSource(String dataSource){

        return treatmentSummaryRepository.findStudyNumberByDataSource(dataSource);
    }


    public String getPlatformUrlByDataSource(String dataSource){

        return treatmentSummaryRepository.findPlatformUrlByDataSource(dataSource);
    }
}
