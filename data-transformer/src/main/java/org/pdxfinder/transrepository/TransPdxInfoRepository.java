package org.pdxfinder.transrepository;

import org.pdxfinder.transdatamodel.PdmrPdxInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransPdxInfoRepository extends JpaRepository<PdmrPdxInfo, Long>
{

    @Query(value = "Select pdmr from PdmrPdxInfo pdmr GROUP BY pdmr.modelID ") //ORDER BY pdmr.clinicalDiagnosis ASC
    List<PdmrPdxInfo> findAllNoDuplicate();

    PdmrPdxInfo findByModelID(String modelID);


}
