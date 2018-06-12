package org.pdxfinder.services;

import org.pdxfinder.dao.TreatmentSummary;
import org.pdxfinder.repositories.DrugRepository;
import org.pdxfinder.repositories.ResponseRepository;
import org.pdxfinder.repositories.TreatmentProtocolRepository;
import org.pdxfinder.repositories.TreatmentSummaryRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by csaba on 29/03/2018.
 */
@Service
public class DrugService {

    private TreatmentSummaryRepository treatmentSummaryRepository;
    private TreatmentProtocolRepository treatmentProtocolRepository;
    private ResponseRepository responseRepository;
    private DrugRepository drugRepository;

    public DrugService(TreatmentSummaryRepository treatmentSummaryRepository,
                       TreatmentProtocolRepository treatmentProtocolRepository,
                       ResponseRepository responseRepository,
                       DrugRepository drugRepository) {

        this.treatmentSummaryRepository = treatmentSummaryRepository;
        this.treatmentProtocolRepository = treatmentProtocolRepository;
        this.responseRepository = responseRepository;
        this.drugRepository = drugRepository;
    }


    public int getDosingStudiesNumberByDataSource(String dataSource){

        return treatmentSummaryRepository.findStudyNumberByDataSource(dataSource);
    }


    public String getPlatformUrlByDataSource(String dataSource){

        return treatmentSummaryRepository.findPlatformUrlByDataSource(dataSource);
    }

    public List<String> getDrugNames(){

        return drugRepository.findDistinctDrugNames();
    }

    public List<String> getResponseOptions(){

        return responseRepository.findAllResponses();
    }

    public List<TreatmentSummary> getSummariesWithDrugAndResponse(){

        return treatmentSummaryRepository.findAllWithDrugData();
    }

    public int getTotalSummaryNumber(){

        return treatmentSummaryRepository.findTotalSummaryNumber();
    }

}
