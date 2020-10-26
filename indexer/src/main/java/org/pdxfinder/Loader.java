package org.pdxfinder;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class Loader {

    public static void main(String[] args) throws Exception {
        new SpringApplicationBuilder(Loader.class)
            .web(WebApplicationType.NONE)
            .run(args);
    }

}
