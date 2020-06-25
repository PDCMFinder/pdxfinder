package org.pdxfinder.configuration;

import org.apache.commons.io.FileUtils;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.transaction.Neo4jTransactionManager;
import org.springframework.data.transaction.ChainedTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;

@Configuration
@EnableNeo4jRepositories(
    basePackages = "org.pdxfinder.graph.repositories",
    transactionManagerRef = "neo4jTransactionManager")
@EnableJpaRepositories(
    basePackages = "org.pdxfinder.rdbms.repositories",
    transactionManagerRef = "h2TransactionManager")
@EnableTransactionManagement
public class DatasourceConfig {

  @Value("${spring.data.neo4j.uri}")
  private String embeddedDataDir;

  @Value("${db-cache-dir}")
  private String cacheDataDir;

  @Value("${db-refresh}")
  private boolean embeddedDbRefresh;

  @Value("${data-dir}")
  private String dataDir;

  @Bean
  public SessionFactory sessionFactory() throws IOException {
    refreshEmbeddedDBFromCache();
    return new SessionFactory(neo4jConfiguration(), "org.pdxfinder.graph");
  }

  private org.neo4j.ogm.config.Configuration neo4jConfiguration() {
    return new org.neo4j.ogm.config.Configuration.Builder()
        .uri(embeddedDataDir)
        .autoIndex("assert")
        .build();
  }

  @Primary
  @Bean(name = "neo4jTransactionManager")
  public Neo4jTransactionManager transactionManager() throws IOException {
    return new Neo4jTransactionManager(sessionFactory());
  }


  /*************************************************************************************************************
   *     RELATIONAL DATABASE CONFIGURATION           *
   **************************************************/

  @Bean
  @ConfigurationProperties("h2.datasource")
  public DataSource dataSource() {
      String url = String.format(
          "jdbc:h2:%s/h2-db/data;" +
          "AUTO_SERVER=true;" +
          "DB_CLOSE_ON_EXIT=FALSE",
          dataDir
      );
      return DataSourceBuilder.create()
          .driverClassName("org.h2.Driver")
          .url(url)
          .username("neo4j")
          .password("neo5j")
          .build();
  }

  @Bean
  public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
    return new PersistenceExceptionTranslationPostProcessor();
  }

  @Autowired
  @Bean
  public JpaTransactionManager h2TransactionManager(
      LocalContainerEntityManagerFactoryBean entityManagerFactory
  ) throws Exception {
    return new JpaTransactionManager(entityManagerFactory.getObject());
  }


  /*************************************************************************************************************
   *     CHAINED TRANSACTION MANAGER FOR MULTIPLE DATA SOURCES         *
   *******************************************************************/

  @Autowired
  @Bean(name = "transactionManager")
  public PlatformTransactionManager transactionManager(
      Neo4jTransactionManager neo4jTransactionManager,
      JpaTransactionManager h2TransactionManager
  ) {
    return new ChainedTransactionManager(
            h2TransactionManager,
            neo4jTransactionManager
    );
  }


  private void refreshEmbeddedDBFromCache() throws IOException{

    boolean dataDeleted = false;
    String dataDir = embeddedDataDir.replace("file://", "");

    if (embeddedDbRefresh) {
      File data = new File(dataDir);
      File cache = new File(cacheDataDir);

      if (data.isDirectory()){
        FileUtils.cleanDirectory(data);
        dataDeleted = true;
      }

      if (cache.isDirectory() && dataDeleted){
        FileUtils.copyDirectory(cache, data);
      }
    }
  }

}


