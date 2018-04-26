package rocketsolrapp.clientapi.service;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rocketsolrapp.clientapi.model.RequestWithParams;
import rocketsolrapp.clientapi.model.rule.Action;
import rocketsolrapp.clientapi.model.rule.Trigger;
import rocketsolrapp.clientapi.schema.RulesQueryService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RuleService {

    private static final String CORE = "rules";

    private static final String FILTER_TYPE = "filter";
    private static final String SORT_TYPE = "sort";
    private static final String REDIRECT_TYPE = "redirect";


    @Autowired
    SolrRequester solr;

    @Autowired
    RulesQueryService queryService;

    public void add(List<Action> actions) throws Exception{
        final UpdateRequest request = new UpdateRequest();
        request.add(actions
                .stream()
                .map(this::concertToSolrFormat)
                .collect(Collectors.toSet()));
        solr.sendSolrRequest(CORE, request);
    }

    public void clear() throws Exception {
        final UpdateRequest request = new UpdateRequest();
        request.deleteByQuery("*:*");
        solr.sendSolrRequest(CORE, request);
    }

    public List<Action> getActions(RequestWithParams request){
        final String keywords = request.getKeywords();
        final SolrQuery query = queryService.buildQuery(keywords, request.getFilter());
        final QueryResponse response = solr.executeQuery(CORE, query);
        return response.getResults()
                .stream()
                .map(this::convertFromSolrFormat)
                .collect(Collectors.toList());
    }

    private SolrInputDocument concertToSolrFormat(Action action) {
        final SolrInputDocument inputDocument = new SolrInputDocument();
        inputDocument.addField("value", action.getValue());
        inputDocument.addField("type", action.getType());
        inputDocument.addField("id", action.getId());
        inputDocument.addField("docType", action.getDocType());
        inputDocument.addField("priority", action.getPriority());
        inputDocument.addChildDocuments(action.getTriggers()
                .stream()
                .map(this::concertToSolrFormat)
                .collect(Collectors.toList()));
        return inputDocument;
    }

    private SolrInputDocument concertToSolrFormat(Trigger trigger) {
        final SolrInputDocument inputDocument = new SolrInputDocument();
        inputDocument.addField("id", trigger.getId());
        inputDocument.addField("filterId", trigger.getFilterId());
        inputDocument.addField("matchmode", trigger.getMatchmode());
        inputDocument.addField("searchTerms", trigger.getSearchTerms());
        inputDocument.addField("exactLocation", trigger.isExactLocation());
        inputDocument.addField("docType", trigger.getDocType());
        inputDocument.addField("filtersCount", trigger.getFilterId().size());
        return inputDocument;
    }

    private Action convertFromSolrFormat(SolrDocument document) {
        final Action action = new Action();
        action.setPriority((long) document.getFieldValue("priority"));
        action.setType((String) document.getFieldValue("type"));
        action.setValue((String) document.getFieldValue("value"));
        return action;
    }

}
