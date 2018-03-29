package rocketsolrapp.clientapi.factory;

import org.apache.solr.client.solrj.SolrClient;

public interface SolrClientFactory {

    SolrClient getClient(String coreName);

}
