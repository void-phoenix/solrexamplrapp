package rocketsolrapp.embedded.factory;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.servlet.SolrDispatchFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import rocketsolrapp.solr.factory.SolrClientConfig;
import rocketsolrapp.solr.factory.SolrRemoteClientFactory;

import javax.annotation.PreDestroy;
import java.io.IOException;

@EnableConfigurationProperties(SolrClientConfig.class)
@Component("solrClient")
public class SolrEmbeddedClientFactory extends SolrRemoteClientFactory {

    private static final Logger LOG = LoggerFactory.getLogger(SolrEmbeddedClientFactory.class);

    @Autowired
    private SolrClientConfig solrClientConfig;

    @Autowired
    private SolrDispatchFilter filter;

    @Override
    public SolrClient getClient(String coreName) {
        if (SolrClientConfig.SolrJtype.EMBEDDED == solrClientConfig.getSolrJtype()) {
            SolrClient solrClient = clients.get(coreName);
            if (solrClient != null) {
                return solrClient;
            }
            clients.putIfAbsent(coreName,
                    new EmbeddedSolrServer(
                            filter.getCores(),
                            coreName
                    )
            );
            return getClient(coreName);
        } else {
            return super.getClient(coreName);
        }
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
