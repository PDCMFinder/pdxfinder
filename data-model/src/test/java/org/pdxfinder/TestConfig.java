package org.pdxfinder;

import org.neo4j.ogm.session.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.transaction.Neo4jTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Test configuration sets up the embedded Neo4J driver in memory mode
 */
@Configuration
@EnableTransactionManagement
@ComponentScan(value = "org.pdxfinder")
@EnableNeo4jRepositories("org.pdxfinder.repositories")
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

}
