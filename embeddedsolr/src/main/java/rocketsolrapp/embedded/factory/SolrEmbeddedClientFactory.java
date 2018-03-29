package rocketsolrapp.embedded.factory;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.servlet.SolrDispatchFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rocketsolrapp.clientapi.factory.SolrClientFactory;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component("embedded")
public class SolrEmbeddedClientFactory implements SolrClientFactory {

    private static final Logger LOG = LoggerFactory.getLogger(SolrEmbeddedClientFactory.class);

    @Autowired
    SolrDispatchFilter filter;

    private Map<String, SolrClient> clients = new HashMap<>();

    @Override
    public SolrClient getClient(String coreName) {
        if (clients.containsKey(coreName)) return clients.get(coreName);
        final CoreContainer coreContainer = filter.getCores();

        coreContainer.waitForLoadingCoresToFinish(10000);
        SolrClient client = new EmbeddedSolrServer(coreContainer, coreName);
        clients.put(coreName, client);
        return client;
    }

    @PreDestroy
    private void cleanup() {
        clients.values().forEach(client -> {
            try {
                client.close();
            } catch (IOException ex) {
                LOG.error(ex.getMessage());
            }
        });
    }
}
