package org.pdxfinder.services.email;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/*
 * Created by abayomi on 16/08/2019.
 */
@Configuration
public class EmailConfig {

    private String host = "mail.pdxfinder.org";
    private String username = "";
    private String password = "";
    private Integer port = 26;
    private String protocol = "smtp";

    private Boolean auth = true;
    private Boolean starttlsEnable = true;
    private String defaultEncoding = "UTF-8";


    @Bean("pdxfinderMailBean")
    public JavaMailSenderImpl javaMailSender() {
        //JavaMailSender
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPassword(password);
        mailSender.setUsername(username);
        mailSender.setPort(port);
        mailSender.setProtocol(protocol);
        mailSender.setJavaMailProperties(getMailProperties());
        mailSender.setDefaultEncoding(defaultEncoding);

        return mailSender;
    }

    private Properties getMailProperties() {
        Properties props = new Properties();
        props.put(EmailConstant.SMTP_AUTH, auth);
        props.put(EmailConstant.SMTP_STARTLS, starttlsEnable);
        return props;
    }
}

