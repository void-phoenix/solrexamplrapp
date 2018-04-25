package rocketsolrapp.clientapi.schema;

import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.stereotype.Service;

@Service
public class RulesQueryService {

    private static final String BASE_QUERY = "{!parent which=docType:action v=$textquery}";
    private static final String TEXTQUERY = "(+{!term f=searchTerms_exact v=$keywords} +matchmode:MatchExact) (+matchmode:matchPhrase +{!lucene qf=searchTerms v=$keywords_phrase}) (+matchmode:MatchAny +{!lucene qf=searchTerms v=$keywords})";
    private static final String TEXTQUERY_PARAM_NAME = "textquery";
    private static final String KEYWORDS_PARAM_NAME = "keywords";
    private static final String KEYWORDS_PHRASE_PARAM_NAME = "keywords_phrase";

    public SolrQuery buildQuery(String keywords) {
        SolrQuery query = new SolrQuery(BASE_QUERY);
        query.add(TEXTQUERY_PARAM_NAME, TEXTQUERY);
        query.add(KEYWORDS_PARAM_NAME, keywords);
        query.add(KEYWORDS_PHRASE_PARAM_NAME, "\"" + keywords + "\"");
        return query;
    }


}
