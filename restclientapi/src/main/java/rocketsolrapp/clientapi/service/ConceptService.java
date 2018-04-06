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
    public static final String CONCEPT_TYPE = "concept";
    public static final String SYNONYM_TYPE = "synonym";

    @Autowired
    SolrRequester solr;

    public NamedList getConcepts(String input)  throws Exception {
        final ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("df", "searchTerms");
        params.set("matchText", "true");
        final SolrTaggerRequest request = new SolrTaggerRequest(params, SolrRequest.METHOD.GET);
        request.setPath("/concepts");
        request.setInput(input);

        return solr.sendSolrRequest( CORE_NAME, request);
    }

}
