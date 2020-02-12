package org.pdxfinder.dataloaders;

import org.pdxfinder.reportmanager.ReportManager;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;

/*
 * Created by csaba on 14/05/2019.
 */
@Component("LoadUniversal")
public class LoadUniversal implements ApplicationContextAware {

//    @Value("${FinderCommandLine.dataDirectory}")
//    private Path finderRootDir;

    @Value("${pdxfinder.root.dir}")
    private String finderRootDir;

    Logger log = LoggerFactory.getLogger(LoadUniversal.class);

    static ApplicationContext context;
    ReportManager reportManager;

    @Autowired private DataImportService dataImportService;
    @Autowired private UtilityService utilityService;

    public void run() throws Exception {

        reportManager = (ReportManager) context.getBean("ReportManager");
        File updogDirectory = new File(finderRootDir + "/data/UPDOG/");

        if (updogDirectory.exists()) {
            File[] updogDirs = updogDirectory.listFiles();
            if (updogDirs.length == 0) {
                log.warn("No subdirs found for the universal loader, skipping");
            } else {

                for (int i = 0; i < updogDirs.length; i++) {

                    if (updogDirs[i].isDirectory()) {

                        String updogCurrDir = finderRootDir + "/data/UPDOG/" + updogDirs[i].getName();

                        log.info("* Starting universal loader");
                        log.info("Loading data from "+updogCurrDir);
                        UniversalLoader updog = new UniversalLoader(reportManager, utilityService, dataImportService);
                        updog.setFinderRootDir(finderRootDir);
                        updog.initTemplates(updogCurrDir);
                        updog.loadTemplateData();

                        log.info("Finished running universal loader");

                    }
                }
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
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

}
