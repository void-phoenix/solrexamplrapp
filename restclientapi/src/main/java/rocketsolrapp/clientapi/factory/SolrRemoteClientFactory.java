package rocketsolrapp.clientapi.factory;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component("remote")
public class SolrRemoteClientFactory implements SolrClientFactory{

    private static final Logger LOG = LoggerFactory.getLogger(SolrRemoteClientFactory.class);

    private Map<String, SolrClient> clients = new HashMap<>();

    @Override
    public SolrClient getClient(String coreName){
        if (clients.containsKey(coreName)) return clients.get(coreName);
        HttpSolrClient client = new HttpSolrClient.Builder().withBaseSolrUrl("http://localhost:8080/" + coreName + "/").build();
        clients.put(coreName, client);
        return client;
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
}
