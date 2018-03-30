package rocketsolrapp.embedded.solr.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.WebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.AbstractServletWebServerFactory;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class SolrWebappRootSetter implements WebServerFactoryCustomizer {

    Logger LOG = LoggerFactory.getLogger(SolrWebappRootSetter.class);

    @Value("${solr-config.home}")
    private String solrHome;


    @Override
    public void customize(WebServerFactory factory) {
        final String webAppDir = solrHome + "/webapp";
        LOG.info("WEBAPP dir: " + webAppDir);
        ((AbstractServletWebServerFactory) factory).setDocumentRoot(new File(webAppDir));
    }
}