package org.pdxfinder.commandline;

import org.apache.commons.collections4.CollectionUtils;
import org.pdxfinder.dataloaders.LoadJAXData;
import org.pdxfinder.dataloaders.updog.Updog;
import org.pdxfinder.mapping.InitMappingDatabase;
import org.pdxfinder.mapping.LinkSamplesToNCITTerms;
import org.pdxfinder.mapping.LinkTreatmentsToNCITTerms;
import org.pdxfinder.postload.CreateDataProjections;
import org.pdxfinder.postload.SetDataVisibility;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.constants.DataProvider;
import org.pdxfinder.services.constants.DataProviderGroup;
import org.pdxfinder.services.constants.DataUrl;
import org.pdxfinder.services.loader.envload.LoadMarkers;
import org.pdxfinder.services.loader.envload.LoadNCIT;
import org.pdxfinder.services.loader.envload.LoadNCITDrugs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
public class FinderLoader {

    // Cache loading Components
    private LoadMarkers loadMarkers;
    private LoadNCITDrugs loadNCITDrugs;
    private LoadNCIT loadNCIT;

    // DataProvider Loading Components
    private LoadJAXData loadJAXData;
    private Updog updog;

    // PostLoad Components
    private LinkSamplesToNCITTerms linkSamplesToNCITTerms;
    private LinkTreatmentsToNCITTerms linkTreatmentsToNCITTerms;
    private CreateDataProjections createDataProjections;
    private SetDataVisibility setDataVisibility;

    private InitMappingDatabase initMappingDatabase;

    private DataImportService dataImportService;

    @Autowired
    public FinderLoader(LoadMarkers loadMarkers,
                        LoadNCITDrugs loadNCITDrugs,
                        LoadNCIT loadNCIT,
                        LoadJAXData loadJAXData,
                        Updog updog,

                        LinkSamplesToNCITTerms linkSamplesToNCITTerms,
                        LinkTreatmentsToNCITTerms linkTreatmentsToNCITTerms,
                        CreateDataProjections createDataProjections,
                        SetDataVisibility setDataVisibility,
                        DataImportService dataImportService,
                        ApplicationContext applicationContext,
                        InitMappingDatabase initMappingDatabase) {

        this.loadMarkers = loadMarkers;
        this.loadNCITDrugs = loadNCITDrugs;
        this.loadNCIT = loadNCIT;

        this.loadJAXData = loadJAXData;
        this.updog = updog;

        this.linkSamplesToNCITTerms = linkSamplesToNCITTerms;
        this.linkTreatmentsToNCITTerms = linkTreatmentsToNCITTerms;
        this.createDataProjections = createDataProjections;
        this.setDataVisibility = setDataVisibility;

        this.initMappingDatabase = initMappingDatabase;

        this.dataImportService = dataImportService;
    }

    private Logger log = LoggerFactory.getLogger(FinderLoader.class);
    @Value("${data-dir}") private String predefDataDirectory;
    @Value("${spring.data.neo4j.uri}") private File databaseURI;
    @Value("${ncitpredef.file}") private String ncitFile;

    void run(
        List<DataProvider> dataProviders,
        File dataDirectory,
        boolean validateOnlyRequested,
        boolean loadCacheRequested,
        boolean postLoadRequested,
        boolean initializeMappingDb
    ) {

        loadCache(loadCacheRequested);
        loadRequestedPdxData(dataProviders, dataDirectory, validateOnlyRequested);
        postLoad(dataProviders, postLoadRequested);
        initializeMappingDb(initializeMappingDb);
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
        if (dataImportService.ontologyCacheIsEmptyByType("treatment") || loadCacheRequested) {
            try {
                loadNCITDrugs.loadRegimens();
            } catch (Exception e) {
                log.error("Failed to load regimen ontology terms", e);
            }
        }
    }

    private void loadRequestedPdxData(
        List<DataProvider> providers,
        File dataDirectory,
        boolean validateOnlyRequested
    ) {
        if (providers.isEmpty()) {
            log.info("Skipping PDX dataset loading - No providers requested");
        } else {
            log.info("Running requested PDX dataset loaders {}...", providers);
            for (DataProvider i : providers)
                callRelevantLoader(i, dataDirectory, validateOnlyRequested);
        }
    }


    private void callRelevantLoader(
        DataProvider dataProvider,
        File dataDirectory,
        boolean validateOnlyRequested
    ) {
        List<DataProvider> updogProviders = DataProviderGroup.getProvidersFrom(DataProviderGroup.UPDOG);
        try {
            if (dataProvider.equals(DataProvider.JAX) && !validateOnlyRequested) {
                loadJAXData.run();
            }
            else{
                    if (updogProviders.contains(dataProvider)) {
                        Path updogDirectory = Paths.get(
                            dataDirectory.toString(),
                            "/data/UPDOG",
                            dataProvider.toString());
                        updog.run(updogDirectory, dataProvider.toString(), validateOnlyRequested);
                    }
            }

        } catch (Exception e) {
            log.error("Error calling the loader for {}:", dataProvider, e);
        }
    }

    private void postLoad(List<DataProvider> providers, boolean postLoadRequested) {

        log.info("Running Post load Steps ...");


        if (CollectionUtils.isNotEmpty(providers) || postLoadRequested) {
            linkSamplesToNCITTerms.run();
            linkTreatmentsToNCITTerms.run();
            createDataProjections.run();
            setDataVisibility.run();
        }
    }
    
    private void initializeMappingDb(boolean initMappingDb){
        if (initMappingDb)
            initMappingDatabase.run();
    }


}
