package org.pdxfinder.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.neo4j.transaction.Neo4jTransactionManager;
import org.springframework.data.transaction.ChainedTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
    basePackages = "org.pdxfinder.rdbms.repositories",
    transactionManagerRef = "h2TransactionManager")
@EnableTransactionManagement
public class H2Configuration {

  @Value("${data-dir}")
  private String dataDir;

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
  ) {
    return new JpaTransactionManager(entityManagerFactory.getObject());
  }

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


}


