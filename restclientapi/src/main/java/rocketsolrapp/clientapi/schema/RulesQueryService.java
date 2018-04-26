package rocketsolrapp.clientapi.schema;

import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RulesQueryService {

    private static final String PARENT_QUERY = "{!parent which=docType:action v=$basequery}";
    private static final String BASE_QUERY = "(+{!term f=searchTerms_exact v=$keywords} +matchmode:MatchExact) (+matchmode:matchPhrase +{!lucene qf=searchTerms v=$keywords_phrase}) (+matchmode:MatchAny +{!lucene qf=searchTerms v=$keywords})";
    private static final String BASE_QUERY_FILTER_ADDITION = " (!filtersCount:0 +exactLocation:false  +{!frange l=0 u=0 incl=true incu=true v=$filter_part})";
    private static final String BASE_QUERY_PARAM_NAME = "basequery";
    private static final String KEYWORDS_PARAM_NAME = "keywords";
    private static final String KEYWORDS_PHRASE_PARAM_NAME = "keywords_phrase";
    private static final String FILTER_PART_PARAM_NAME = "filter_part";

    public SolrQuery buildQuery(String keywords, List<String> filters) {
        SolrQuery query = new SolrQuery(PARENT_QUERY);
        final String baseQuery = filters.isEmpty() ? BASE_QUERY : BASE_QUERY + BASE_QUERY_FILTER_ADDITION;
        query.add(BASE_QUERY_PARAM_NAME, baseQuery);
        query.add(KEYWORDS_PARAM_NAME, keywords);
        query.add(KEYWORDS_PHRASE_PARAM_NAME, "\"" + keywords + "\"");
        buildFilterPartQuery(filters, query);
        return query;
    }

    private void buildFilterPartQuery(List<String> filters, SolrQuery query){
        if (filters.isEmpty()) return ;
        final StringBuilder result = new StringBuilder();
        result.append("sub( sum( ");
        List<String> filterRequests = new ArrayList<>(filters.size());
        for (String filter : filters) {
            filterRequests.add("if(exists(query({!v=filterId:\"" + filter + "\"})), 1, 0)");
        }
        result.append(filterRequests.stream().collect(Collectors.joining(", ")));
        result.append(" ), field(filtersCount))");
        query.add(FILTER_PART_PARAM_NAME, result.toString());
    }


}
