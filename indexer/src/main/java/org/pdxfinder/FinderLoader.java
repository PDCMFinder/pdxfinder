package org.pdxfinder;

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
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
public class FinderLoader {

    private LoadMarkers loadMarkers;
    private LoadNCITDrugs loadNCITDrugs;
    private LoadNCIT loadNCIT;
    private DataImportService dataImportService;

    @Autowired
    public FinderLoader(
        LoadMarkers loadMarkers,
        LoadNCIT loadNCIT,
        LoadNCITDrugs loadNCITDrugs,
        DataImportService dataImportService
    ) {
        this.loadMarkers = loadMarkers;
        this.loadNCIT = loadNCIT;
        this.loadNCITDrugs = loadNCITDrugs;
        this.dataImportService = dataImportService;
    }

    private Logger log = LoggerFactory.getLogger(FinderLoader.class);
    @Value("${data-dir}") private String predefDataDirectory;
    @Value("${spring.data.neo4j.uri}") private File databaseURI;
    @Value("${ncitpredef.file}") private String ncitFile;

    void run(
        List<DataProvider> dataProviders,
        File dataDirectory,
        boolean clearCacheRequested,
        boolean keepDatabaseRequested
        ) {
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

    private void callRelevantLoader(DataProvider dataProvider) {
        try {
            log.debug("Loading data for {}", dataProvider);
            dataProvider.load();
        } catch (Exception e) {
            log.error("Error calling loader for {}:", dataProvider, e);
        }
    }

}
