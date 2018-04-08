package rocketsolrapp.clientapi.service;

import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rocketsolrapp.clientapi.model.Concept;
import rocketsolrapp.clientapi.model.SolrTaggerRequest;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConceptService {

    public static final String CONCEPT_TYPE = "concept";
    public static final String SYNONYM_TYPE = "synonym";
    private static final String CORE_NAME = "concepts";
    @Autowired
    SolrRequester solr;

    public NamedList getConcepts(String input) throws Exception {
        final ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("df", "searchTerms");
        params.set("matchText", "true");
        final SolrTaggerRequest request = new SolrTaggerRequest(params, SolrRequest.METHOD.GET);
        request.setPath("/concepts");
        request.setInput(input);

        return solr.sendSolrRequest(CORE_NAME, request);
    }

    public NamedList getSynonyms(String input) throws Exception {
        final ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("df", "searchTerms");
        params.set("matchText", "true");
        params.set("fq", "type:" + SYNONYM_TYPE);
        final SolrTaggerRequest request = new SolrTaggerRequest(params, SolrRequest.METHOD.GET);
        request.setPath("/concepts");
        request.setInput(input);

        return solr.sendSolrRequest(CORE_NAME, request);
    }

    public void add(List<Concept> concepts) throws Exception {
        final List<SolrInputDocument> inputDocuments = concepts.stream().
                map(this::concertToSolrFormat).
                collect(Collectors.toList());
        final UpdateRequest request = new UpdateRequest();
        request.add(inputDocuments);
        solr.sendSolrRequest(CORE_NAME, request);
    }

    private SolrInputDocument concertToSolrFormat(Concept concept) {
        final SolrInputDocument inputDocument = new SolrInputDocument();
        inputDocument.addField("field", concept.getField());
        inputDocument.addField("searchTerms", concept.getSearchTerms());
        inputDocument.addField("synonyms", concept.getSynonyms());
        inputDocument.addField("type", concept.getType());
        return inputDocument;
    }

}
