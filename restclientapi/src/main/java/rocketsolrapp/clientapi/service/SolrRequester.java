package rocketsolrapp.clientapi.service;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import rocketsolrapp.clientapi.factory.SolrRemoteClientFactory;

import java.io.IOException;

@Service
public class SolrRequester {

    private final static Logger LOG = LoggerFactory.getLogger(SolrRequester.class);

    @Autowired()
    @Qualifier("remote")
    SolrRemoteClientFactory solrClientFactory;

    public NamedList sendSolrRequest(String coreName, SolrRequest solrRequest)
            throws IOException, SolrServerException {
        SolrClient solrClient = solrClientFactory.getClient(coreName);
        return solrClient.request(solrRequest);
    }

    public QueryResponse executeQuery(final String coreName,
                                       final ModifiableSolrParams query) {
        SolrClient solrClient = solrClientFactory.getClient(coreName);
        try {
            return solrClient.query(query, SolrRequest.METHOD.POST);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Error while execute solr query: " + e.getMessage(), e);
        }
    }

}
