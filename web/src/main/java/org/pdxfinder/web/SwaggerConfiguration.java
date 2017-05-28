package org.pdxfinder.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
@Import({springfox.documentation.spring.data.rest.configuration.SpringDataRestConfiguration.class})
public class SwaggerConfiguration {


    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.ant("/api/**"))
                .build();
    }


    private ApiInfo apiInfo() {

        return new ApiInfoBuilder()
                .title("PDX finder application programming interface (API) documentation")
                .description("This is the PDX finder API documentation.  The PDX finder portal provides a set of endpoints that allows developers to connect to the resource programmatically.<br/ ><br /><b>Note that PUT, POST, and DELETE requests must be authenticated.</b>")
                .termsOfServiceUrl("http://www.pdxfinder.org")
                .contact(new Contact("PDX Finder -- A collaboration between The Jackson Laboratory and the European Bioinformatics Institute.  This work is supported by the National Institutes of Health U24 CA204781 01 and the National Cancer Institute MTB R01 R01CA089713.", "http://www.pdxfinder.org/contact", "helpdesk@pdxfinder.org"))
                .license("Apache License Version 2.0")
                .licenseUrl("https://github.com/PDXFinder/pdxfinder/blob/master/LICENSE")
                .version("2.0")
                .build();
    }

}