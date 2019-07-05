package org.pdxfinder.commands.dataloaders;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContextAware;


import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;


import org.neo4j.ogm.session.Session;

import org.pdxfinder.reportmanager.ReportManager;
import org.pdxfinder.services.DataImportService;

import org.pdxfinder.services.UtilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.File;

/*
 * Created by csaba on 14/05/2019.
 */
@Component
@Order(value = 0)
public class LoadUniversal implements CommandLineRunner, ApplicationContextAware{


    @Value("${pdxfinder.root.dir}")
    private String finderRootDir;

    Logger log = LoggerFactory.getLogger(LoadUniversal.class);


    private Options options;
    private CommandLineParser parser;
    private CommandLine cmd;
    private HelpFormatter formatter;

    static ApplicationContext context;
    ReportManager reportManager;


    @Autowired
    private DataImportService dataImportService;

    @Autowired
    private UtilityService utilityService;

    private Session session;


    @Override
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("loadUniversal", "Running universal loader (UPDOG)");
        parser.accepts("loadALL", "Load all, running universal loader (UPDOG)");
        OptionSet options = parser.parse(args);

        if (options.has("loadUniversal") || options.has("loadALL")) {

            reportManager = (ReportManager) context.getBean("ReportManager");

            File folder = new File(finderRootDir +"/data/UPDOG/");

            if (folder.exists()) {

                File[] updogDirs = folder.listFiles();

                if (updogDirs.length == 0) {

                    log.warn("No subdirs found for the universal loader, skipping");
                } else {

                    for (int i = 0; i < updogDirs.length; i++) {

                        if (updogDirs[i].isDirectory()) {

                            String templateFileStr = finderRootDir + "/data/UPDOG/" + updogDirs[i].getName() + "/template.xlsx";

                            File template = new File(templateFileStr);

                            //found the template, load it
                            if (template.exists()) {



                                log.info("******************************************************");
                                log.info("* Starting universal loader                          *");
                                log.info("******************************************************");

                                UniversalLoader updog = new UniversalLoader(reportManager, utilityService, dataImportService);
                                updog.setFinderRootDir(finderRootDir);
                                updog.initTemplate(templateFileStr);
                                updog.loadTemplateData();

                                log.info("******************************************************");
                                log.info("* Finished running universal loader                  *");
                                log.info("******************************************************");

                            } else {

                                log.error("No template file found for universal loader in " + updogDirs[i]);
                            }

                        }
                    }
                }

            }
            //NO UNIVERSAL TEMPLATES, SKIP
            else {

                log.warn("No UPDOG directory found. Who let the dog out?");
            }


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
