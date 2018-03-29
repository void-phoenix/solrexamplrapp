package rocketsolrapp.solr.factory;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@EnableConfigurationProperties(SolrClientConfig.class)
@Component("solrClient")
public class SolrRemoteClientFactory implements SolrClientFactory{

    @Autowired
    private SolrClientConfig solrClientConfig;

    private static final Logger LOG = LoggerFactory.getLogger(SolrRemoteClientFactory.class);

    protected ConcurrentHashMap<String, SolrClient> clients = new ConcurrentHashMap<>();

    @Override
    public SolrClient getClient(final String coreName) {
        SolrClient solrClient = clients.get(coreName);
        if (solrClient != null) {
            return solrClient;
        }
        switch (solrClientConfig.getSolrJtype()) {
            case CLOUD:
                LOG.info("used cloud solr");
                solrClient = generateCloudClient();
                break;
            case HTTP:
                LOG.info("used http solr");
                solrClient = generateHttpClient(coreName);
                break;
            default:
                throw new IllegalArgumentException("Couldn't create client for type '"+solrClientConfig.getSolrJtype()+"' and core " +
                        "'"+coreName+"'");
        }
        clients.putIfAbsent(coreName, solrClient);
        return solrClient;
    }

    @PreDestroy
    private void cleanup(){
        clients.values().forEach(client -> {
            try {
                client.close();
            } catch (IOException ex){
                LOG.error(ex.getMessage());
            }
        });
    }

    private SolrClient generateHttpClient(String coreName) {
        return new HttpSolrClient.Builder()
                .allowCompression(false)
                .withBaseSolrUrl(solrClientConfig.getUrlString() + "/" + coreName)
                .build();
    }

    private SolrClient generateCloudClient() {
        return new CloudSolrClient.Builder()
                .withSolrUrl(solrClientConfig.getZkHostString())
                .build();
    }
}
