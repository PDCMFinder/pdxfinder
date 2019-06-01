package org.pdxfinder.commands.postload;

import org.pdxfinder.reportmanager.ReportManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/*
 * Created by csaba on 27/02/2019.
 */
@Component
@Order(value = 93)
public class PrintReport implements CommandLineRunner, ApplicationContextAware {


    private final static Logger log = LoggerFactory.getLogger(PrintReport.class);
    static ApplicationContext context;
    ReportManager reportManager;





    @Override
    public void run(String... strings) throws Exception {


        printReport();
    }


    private void printReport(){

        reportManager = (ReportManager) context.getBean("ReportManager");
        reportManager.printMessages("ERROR");


    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

}
