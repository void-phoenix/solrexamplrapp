package rocketsolrapp;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.solr.SolrAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

import javax.servlet.*;

@SpringBootApplication(exclude = {SolrAutoConfiguration.class})
public class Application {

    @Autowired
    ServletContext servletContext;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
