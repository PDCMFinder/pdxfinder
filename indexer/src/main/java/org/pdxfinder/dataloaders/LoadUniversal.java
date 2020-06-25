package org.pdxfinder.dataloaders;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextAware;
import org.pdxfinder.reportmanager.ReportManager;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import java.io.File;
import java.nio.file.Paths;


@Service
public class LoadUniversal implements ApplicationContextAware {

    @Value("${data-dir}")
    private String finderRootDir;

    private Logger log = LoggerFactory.getLogger(LoadUniversal.class);

    private static ApplicationContext context;

    private ReportManager reportManager;

    @Autowired
    private DataImportService dataImportService;

    @Autowired
    private UtilityService utilityService;


    public void run(String dataProvider) throws Exception {

        finderRootDir = UniversalLoader.stripTrailingSlash(finderRootDir);

        String updogDir = String.format("%s/data/UPDOG", finderRootDir);

        String providerDir = String.format("%s/%s", updogDir, dataProvider.replace("_","-"));

        reportManager = (ReportManager) context.getBean("ReportManager");

        File folder = new File(updogDir);

        if (folder.exists()) {

            File dataDir = Paths.get(providerDir).toFile();

            if (dataDir.isDirectory()) {

                log.info("******************************************************");
                log.info("* Starting universal loader for {}                   *", dataProvider);
                log.info("******************************************************");
                log.info("Loading data from {} ", providerDir);
                UniversalLoader updog = new UniversalLoader(reportManager, utilityService, dataImportService);
                updog.setFinderRootDir(finderRootDir);
                updog.initTemplates(providerDir);
                updog.loadTemplateData();

                log.info("******************************************************");
                log.info("* Finished running universal loader for {}           *", dataProvider);
                log.info("******************************************************");

            }

        }
        //NO UNIVERSAL TEMPLATES, SKIP
        else {

            log.warn("No UPDOG directory found. Who let the dog out?");
        }

    }


    public LoadUniversal(DataImportService dataImportService) {
        this.dataImportService = dataImportService;
    }

    public LoadUniversal() {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.context = applicationContext;
    }

}
