package rocketsolrapp.boot;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.solr.SolrAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("rocketsolrapp.embedded, " +
      "rocketsolrapp.clientapi")
@EnableAutoConfiguration(exclude = {SolrAutoConfiguration.class,
        SecurityAutoConfiguration.class})
public class EmbeddedApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmbeddedApplication.class, args);
    }
}
