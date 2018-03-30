package rocketsolrapp.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.solr.SolrAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("rocketsolrapp.clientapi, " +
        "rocketsolrapp.solr.factory, ")
@EnableAutoConfiguration(exclude = {SolrAutoConfiguration.class})
public class SearchAPIBoot {
    public static void main(String[] args) {
        SpringApplication.run(SearchAPIBoot.class, args);
    }
}