package org.pdxfinder.configuration;

import org.apache.commons.io.FileUtils;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.transaction.Neo4jTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.io.File;
import java.io.IOException;

@Configuration
@EnableNeo4jRepositories(
    basePackages = "org.pdxfinder.graph.repositories",
    transactionManagerRef = "neo4jTransactionManager")
@EnableTransactionManagement
public class Neo4jConfiguration {

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


