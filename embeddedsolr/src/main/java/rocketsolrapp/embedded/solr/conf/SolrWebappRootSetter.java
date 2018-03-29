package rocketsolrapp.embedded.solr.conf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.WebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.AbstractServletWebServerFactory;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class SolrWebappRootSetter implements WebServerFactoryCustomizer {

    @Value("${solr-config.home}")
    private String solrHome;


    @Override
    public void customize(WebServerFactory factory) {
        ((AbstractServletWebServerFactory) factory).setDocumentRoot(new File(solrHome + "/webapp"));
    }
}