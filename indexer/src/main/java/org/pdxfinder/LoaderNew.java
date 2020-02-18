package org.pdxfinder;

import org.pdxfinder.dataloaders.*;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.constants.DataUrl;
import org.pdxfinder.services.loader.envload.LoadMarkers;
import org.pdxfinder.services.loader.envload.LoadNCIT;
import org.pdxfinder.services.loader.envload.LoadNCITDrugs;
import org.pdxfinder.utils.DataProviders.DataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
public class LoaderNew {

    private LoadMarkers loadMarkers;
    private LoadNCITDrugs loadNCITDrugs;
    private LoadNCIT loadNCIT;
    private DataImportService dataImportService;
    private LoadPDMRData loadPDMRData;
    private LoadJAXData loadJAXData;
    private LoadHCI loadHCI;
    private LoadIRCC loadIRCC;
    private LoadMDAnderson loadMDAnderson;
    private LoadWISTAR loadWISTAR;
    private LoadWUSTL loadWUSTL;


    public LoaderNew(LoadMarkers loadMarkers,
                     LoadNCITDrugs loadNCITDrugs,
                     LoadNCIT loadNCIT,
                     DataImportService dataImportService,
                     LoadPDMRData loadPDMRData,
                     LoadJAXData loadJAXData,
                     LoadHCI loadHCI,
                     LoadIRCC loadIRCC,
                     LoadMDAnderson loadMDAnderson,
                     LoadWISTAR loadWISTAR,
                     LoadWUSTL loadWUSTL) {
        this.loadMarkers = loadMarkers;
        this.loadNCITDrugs = loadNCITDrugs;
        this.loadNCIT = loadNCIT;
        this.dataImportService = dataImportService;
        this.loadPDMRData = loadPDMRData;
        this.loadJAXData = loadJAXData;
        this.loadHCI = loadHCI;
        this.loadIRCC = loadIRCC;
        this.loadMDAnderson = loadMDAnderson;
        this.loadWISTAR = loadWISTAR;
        this.loadWUSTL = loadWUSTL;
    }

    private Logger log = LoggerFactory.getLogger(LoaderNew.class);
    @Value("${spring.data.neo4j.uri}") private File databaseURI;
    @Value("${ncitpredef.file}") private String ncitFile;

    void run(
        List<DataProvider> dataProviders,
        String dataDirectory,
        boolean clearCacheRequested,
        boolean keepDatabaseRequested) {

        keepDatabaseIfRequested(keepDatabaseRequested);
        loadOntologyTerms(clearCacheRequested);
        loadRequestedPdxData(dataProviders);
    }

    private void keepDatabaseIfRequested(boolean keepDatabaseRequested) {
        if (keepDatabaseRequested) {
            log.info("Using existing database: {}", databaseURI);
        } else {
            throw new UnsupportedOperationException(
                "Removing the database on load is not yet supported, " +
                    "please use `-k` or `--keep-db` for the time being.");
//            clearDatabase();
        }
    }

    private void clearDatabase() {
        log.info("Deleting all nodes and edges in existing database [{}]", databaseURI);
        try {
            dataImportService.deleteAll();
        } catch (DataAccessException e) {
            log.error("Failed to delete database nodes and edges:", e);
        }
    }

    private void loadOntologyTerms(boolean clearCacheRequested) {
        log.info("Loading cache ...");
        loadMarkers(clearCacheRequested);
        loadDiseaseTerms(clearCacheRequested);
        loadRegimens(clearCacheRequested);
    }

    private void loadMarkers(boolean clearCacheRequested) {
        if (dataImportService.markerCacheIsEmpty() || clearCacheRequested) {
            try {
                loadMarkers.loadGenes(DataUrl.HUGO_FILE_URL.get());
            } catch (Exception e) {
                log.error("Failed to load markers", e);
            }
        }
    }

    private void loadDiseaseTerms(boolean clearCacheRequested) {
        if (dataImportService.ontologyCacheIsEmpty() || clearCacheRequested) {
            try {
                loadNCIT.loadOntology(DataUrl.DISEASES_BRANCH_URL.get());
            } catch (Exception e) {
                log.error("Failed to load disease ontology terms", e);
            }
        }
    }

    private void loadRegimens(boolean clearCacheRequested) {
        if (dataImportService.ontologyCacheIsEmpty() || clearCacheRequested) {
            try {
                loadNCITDrugs.loadRegimens();
            } catch (Exception e) {
                log.error("Failed to load regimen ontology terms", e);
            }
        }
    }

    private void loadRequestedPdxData(List<DataProvider> providers) {

        log.debug("Running requested PDX dataset loaders {}...", providers);
        for (DataProvider i : providers) {
            callRelevantLoader(i);
        }
    }




    public void callRelevantLoader(DataProvider dataProvider) {

        try {

            switch (dataProvider) {

                case PDMR:
                    loadPDMRData.run();
                    break;
                case JAX:
                    loadJAXData.run();
                    break;
                case PDXNet_HCI_BCM:
                    loadHCI.run();
                    break;
                case IRCC_CRC:
                    loadIRCC.run();
                    break;
                case PDXNet_MDAnderson:
                    loadMDAnderson.run();
                    break;
                case PDXNet_Wistar_MDAnderson_Penn:
                    loadWISTAR.run();
                    break;
                case PDXNet_WUSTL:
                    loadWUSTL.run();
                    break;
                default:
                    log.info("Error Loading {}", dataProvider);
            }

        } catch (Exception e) {
            log.error("Error calling loader for {}:", dataProvider, e);
        }





//        try {
//            log.debug("Loading data for {}", dataProvider);
//            dataProvider.load();
//        } catch (Exception e) {
//            log.error("Error calling loader for {}:", dataProvider, e);
//        }
    }

}
