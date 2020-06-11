package org.pdxfinder;


import org.neo4j.ogm.session.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.transaction.Neo4jTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.templateresolver.SpringResourceTemplateResolver;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;


/**
 * Test configuration sets up the embedded Neo4J driver in memory mode
 * And Embedded H2 driver in memory mode
 */
@Configuration
@EnableTransactionManagement
@ComponentScan(value = "org.pdxfinder")
@EnableNeo4jRepositories("org.pdxfinder.graph.repositories")
@EnableJpaRepositories(basePackages = "org.pdxfinder.rdbms.repositories")
public class TestConfig {


  @Bean
  public org.neo4j.ogm.config.Configuration getConfiguration() {
    org.neo4j.ogm.config.Configuration config = new org.neo4j.ogm.config.Configuration();

    // To persist the database, uncomment this section
    //        String pathToDb = Paths.get(".").toAbsolutePath().normalize().toString() + "/target/test_graph.db";
    //        config
    //                .driverConfiguration()
    //                .setDriverClassName("org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver")
    //                .setURI("file://" + pathToDb);
    //
    //        System.out.println(config);

    return config;
  }

  @Bean
  public SessionFactory sessionFactory() {
    return new SessionFactory(getConfiguration(), "org.pdxfinder");
  }

  @Bean
  public Neo4jTransactionManager transactionManager() {
    return new Neo4jTransactionManager(sessionFactory());
  }


  @Bean
  public DataSource dataSource(){
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName("org.h2.Driver");
    dataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
    dataSource.setUsername("neo4j");
    dataSource.setPassword("neo5j");
    return dataSource;
  }


  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
    LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
    entityManagerFactoryBean.setDataSource(dataSource());
    entityManagerFactoryBean.setPackagesToScan("org.pdxfinder.rdbms");

    JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
    entityManagerFactoryBean.setJpaVendorAdapter(vendorAdapter);

    return entityManagerFactoryBean;
  }


  @Bean
  public SpringTemplateEngine springTemplateEngine() {
    SpringTemplateEngine templateEngine = new SpringTemplateEngine();
    templateEngine.addTemplateResolver(htmlTemplateResolver());
    return templateEngine;
  }

  @Bean
  public SpringResourceTemplateResolver htmlTemplateResolver(){
    SpringResourceTemplateResolver emailTemplateResolver = new SpringResourceTemplateResolver();
    emailTemplateResolver.setPrefix("classpath:/templates/");
    emailTemplateResolver.setSuffix(".html");
    //emailTemplateResolver.setTemplateMode();
    emailTemplateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
    return emailTemplateResolver;
  }


  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }


}

