package org.pdxfinder.commands.postload;

import org.pdxfinder.reportmanager.ReportManager;
import org.pdxfinder.services.UtilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/*
 * Created by csaba on 27/02/2019.
 */
@Component
@Order(value = 93)
public class PrintReport implements CommandLineRunner, ApplicationContextAware {


    private final static Logger log = LoggerFactory.getLogger(PrintReport.class);
    static ApplicationContext context;
    ReportManager reportManager;


    @Value("${pdxfinder.root.dir}")
    private String finderRootDir;

    private UtilityService utilityService;

    @Autowired
    public PrintReport(UtilityService utilityService) {
        this.utilityService = utilityService;
    }

    @Override
    public void run(String... strings) throws Exception {


        printReport();
        saveReportInCsv();
    }


    private void printReport(){

        reportManager = (ReportManager) context.getBean("ReportManager");
        reportManager.printMessages("ERROR");


    }


    private void saveReportInCsv(){
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
        String logFile = finderRootDir + "/logs/markerlog_"+timeStamp+".csv";
        List<String> headers = new ArrayList<>();
        headers.addAll(Arrays.asList("Type", "Reporter", "DataSource", "Model", "MolChar", "Platform", "MarkerInFile", "HarmonizedMarker", "ReasonOfChange", "Message"));
        utilityService.writeCsvFile(headers, reportManager.getMarkerHarmonizationMessagesInList(), logFile);

    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

}
