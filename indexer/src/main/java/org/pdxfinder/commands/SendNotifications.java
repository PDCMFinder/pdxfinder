package org.pdxfinder.commands;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/*
 * Created by csaba on 03/07/2018.
 */
@Component
@Order(value = 96)
public class SendNotifications implements CommandLineRunner{


    private final static Logger log = LoggerFactory.getLogger(SendNotifications.class);



    @Override
    public void run(String... args) throws Exception {


        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();


        parser.accepts("loadALL", "Load all, then send email notifications");
        parser.accepts("sendNotifications", "Send email notifications");

        OptionSet options = parser.parse(args);

        if (options.has("loadALL")  || options.has("sendNotifications")) {

            log.info("Creating and sending email notifications");

            getUnmappedSamples();


            sendEmail();
        }

    }




    private void getUnmappedSamples(){



    }

    private void sendEmail(){

    }


}
