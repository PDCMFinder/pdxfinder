package org.pdxfinder;

import org.pdxfinder.dataloaders.LoadAdditionalDatasets;
import org.pdxfinder.mapping.LinkSamplesToNCITTerms;
import org.pdxfinder.mapping.LinkTreatmentsToNCITTerms;
import org.pdxfinder.postload.CreateDataProjections;
import org.pdxfinder.postload.SetDataVisibility;
import org.pdxfinder.postload.ValidateDB;
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

    // Cache loading Components
    private LoadMarkers loadMarkers;
    private LoadNCITDrugs loadNCITDrugs;
    private LoadNCIT loadNCIT;

    // PostLoad Components
    private LoadAdditionalDatasets loadAdditionalDatasets;
    private LinkSamplesToNCITTerms linkSamplesToNCITTerms;
    private LinkTreatmentsToNCITTerms linkTreatmentsToNCITTerms;
    private CreateDataProjections createDataProjections;
    private SetDataVisibility setDataVisibility;
    private ValidateDB validateDB;

    private DataImportService dataImportService;

    @Autowired
    public FinderLoader(LoadMarkers loadMarkers,
                        LoadNCITDrugs loadNCITDrugs,
                        LoadNCIT loadNCIT,
                        LoadAdditionalDatasets loadAdditionalDatasets,
                        LinkSamplesToNCITTerms linkSamplesToNCITTerms,
                        LinkTreatmentsToNCITTerms linkTreatmentsToNCITTerms,
                        CreateDataProjections createDataProjections,
                        SetDataVisibility setDataVisibility,
                        ValidateDB validateDB,
                        DataImportService dataImportService) {

        this.loadMarkers = loadMarkers;
        this.loadNCITDrugs = loadNCITDrugs;
        this.loadNCIT = loadNCIT;

        this.loadAdditionalDatasets = loadAdditionalDatasets;
        this.linkSamplesToNCITTerms = linkSamplesToNCITTerms;
        this.linkTreatmentsToNCITTerms = linkTreatmentsToNCITTerms;
        this.createDataProjections = createDataProjections;
        this.setDataVisibility = setDataVisibility;
        this.validateDB = validateDB;

        this.dataImportService = dataImportService;
    }

    private Logger log = LoggerFactory.getLogger(FinderLoader.class);
    @Value("${data-dir}")
    private String predefDataDirectory;
    @Value("${spring.data.neo4j.uri}")
    private File databaseURI;
    @Value("${ncitpredef.file}")
    private String ncitFile;

    void run(List<DataProvider> dataProviders,
             boolean loadCacheRequested,
             boolean keepDatabaseRequested,
             boolean postLoadRequested) {

        this.keepDatabaseIfRequested(keepDatabaseRequested);
        this.loadCache(loadCacheRequested);
        this.loadRequestedPdxData(dataProviders);
        this.postLoad(dataProviders, postLoadRequested);
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

    private void loadCache(boolean loadCacheRequested) {
        log.info("Loading cache ...");
        loadMarkers(loadCacheRequested);
        loadDiseaseTerms(loadCacheRequested);
        loadRegimens(loadCacheRequested);
    }

    private void loadMarkers(boolean loadCacheRequested) {
        if (dataImportService.markerCacheIsEmpty() || loadCacheRequested) {
            try {
                loadMarkers.loadGenes(DataUrl.HUGO_FILE_URL.get());
            } catch (Exception e) {
                log.error("Failed to load markers", e);
            }
        }
    }

    private void loadDiseaseTerms(boolean loadCacheRequested) {
        if (dataImportService.ontologyCacheIsEmpty() || loadCacheRequested) {
            try {
                loadNCIT.loadOntology(DataUrl.DISEASES_BRANCH_URL.get());
            } catch (Exception e) {
                log.error("Failed to load disease ontology terms", e);
            }
        }
    }

    private void loadRegimens(boolean loadCacheRequested) {
        if (dataImportService.ontologyCacheIsEmpty() || loadCacheRequested) {
            try {
                loadNCITDrugs.loadRegimens();
            } catch (Exception e) {
                log.error("Failed to load regimen ontology terms", e);
            }
        }
    }

    private void loadRequestedPdxData(List<DataProvider> providers) {
        if (providers.isEmpty()){
            log.info("No PDX dataset loading at the moment because user did not request for any");
        }
        log.debug("Running requested PDX dataset loaders {}...", providers);
        for (DataProvider i : providers) {
            callRelevantLoader(i);
        }
    }


    private void postLoad(List<DataProvider> providers, boolean postLoadRequested) {

        log.info("Running Post load Steps ...");

        if (providers.contains(DataProvider.CRL)){
            loadAdditionalDatasets.run();
        }

        if (!providers.isEmpty() || postLoadRequested){

            linkSamplesToNCITTerms.run();
            linkTreatmentsToNCITTerms.run();
            createDataProjections.run();
            setDataVisibility.run();
            validateDB.run();
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
