package rocketsolrapp.clientapi.service;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.NamedList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SpellCheckService {

    @Autowired
    SolrRequester solr;

    public List<String> checkSpelling(String keywords) throws Exception{

        List<String> collationResult = new ArrayList<>();
        final SolrQuery query = new SolrQuery();
        query.set("q", keywords);
        query.setRequestHandler("/spell");

        final QueryResponse queryResponse = solr.executeQuery(ProductService.CORE_NAME, query);
        final NamedList spellcheck = (NamedList)(queryResponse.getResponse()).get("spellcheck");
        final NamedList collations = (NamedList) spellcheck.get("collations");

        for (NamedList collation : (ArrayList<NamedList>) collations.getAll("collation")){
            collationResult.add((String) collation.get("collationQuery"));
        }

        return collationResult;
    }
}
