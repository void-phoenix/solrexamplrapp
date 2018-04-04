package rocketsolrapp.clientapi.service;

import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rocketsolrapp.clientapi.model.SolrTaggerRequest;

@Service
public class ConceptService {

    private static final String CORE_NAME = "concepts";

    @Autowired
    SolrRequester solr;

    public SolrDocumentList getConcepts(String input)  throws Exception {
        final ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("df", "searchTerms");
        final SolrTaggerRequest request = new SolrTaggerRequest(params, SolrRequest.METHOD.GET);
        request.setPath("/concepts");
        request.setInput(input);

        final NamedList response = solr.sendSolrRequest( CORE_NAME, request);
        return (SolrDocumentList) response.get("response");
    }

}
