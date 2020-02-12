package org.pdxfinder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableNeo4jRepositories

@EnableTransactionManagement
public class Loader {

    public static void main(String[] args) throws Exception {
        System.exit(
            SpringApplication.exit(
                SpringApplication.run(Loader.class, args)));

    }

}
