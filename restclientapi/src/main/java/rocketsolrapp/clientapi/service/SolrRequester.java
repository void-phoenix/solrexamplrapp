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
import org.springframework.stereotype.Service;
import rocketsolrapp.solr.factory.SolrClientFactory;

import java.io.IOException;

@Service
public class SolrRequester {

    private final static Logger LOG = LoggerFactory.getLogger(SolrRequester.class);

    @Autowired
    protected SolrClientFactory solrClientFactory;

    public NamedList sendSolrRequest(String coreName, SolrRequest solrRequest)
            throws IOException, SolrServerException {
        return sendSolrRequest(coreName, solrRequest, true);
    }

    public NamedList sendSolrRequest(String coreName, SolrRequest solrRequest, boolean commit)
            throws IOException, SolrServerException {
        SolrClient solrClient = solrClientFactory.getClient(coreName);
        final NamedList result = solrClient.request(solrRequest);
        if (commit) solrClient.commit();
        return result;
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
