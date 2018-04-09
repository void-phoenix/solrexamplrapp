package rocketsolrapp.clientapi.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.NamedList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rocketsolrapp.clientapi.model.Facet;
import rocketsolrapp.clientapi.model.RequestWithParams;

import java.util.ArrayList;
import java.util.List;

@Service
public class FacetService {

    public static final String FACET_PARAM = "json.facet";

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    ProductQueryBuilder productQueryBuilder;

    public String buildFacetRequestPart(RequestWithParams requestWithParams,
                                        List<String> childFields,
                                        List<String> parentFields) throws Exception{
        final ObjectNode facetRequest = mapper.createObjectNode();

        for (String field : childFields) {
            facetRequest.set(field, buildChildToParentFacet(field));
        }

        for (String field : parentFields) {
            facetRequest.set(field, buildParentToChildFacet(field));
        }

        final String jsonFacet = mapper.writeValueAsString(facetRequest);
        return jsonFacet;
    }

    public List<Facet> extractFacets(QueryResponse response){
        final List<String> facetFields = productQueryBuilder.getAllFacetFields();
        final NamedList facetsResponse = (NamedList) response.getResponse().get("facets");
        final List<Facet> facetResult = new ArrayList<>();

        for (String facetField : facetFields) {
            Object facetObject = facetsResponse.get(facetField);
            if (facetObject == null) continue;
            final Facet facet = new Facet();
            facet.setField(facetField);
            ArrayList<NamedList> facetValuesRaw = (ArrayList<NamedList>)((NamedList) facetObject).get("buckets");
            for (NamedList facetRaw : facetValuesRaw) {
                facet.addCount((String) facetRaw.get("val"),
                        (int) facetRaw.get("count"));
            }
            facetResult.add(facet);
        }
        return facetResult;
    }

    private ObjectNode buildChildToParentFacet(String fieldName) {
        final ObjectNode facet = mapper.createObjectNode();

        facet.put("type", "terms");
        facet.put("field", fieldName);

        final ObjectNode domain = mapper.createObjectNode();
        domain.put("blockChildren", "docType:product");

        facet.set("domain", domain);
        return facet;
    }

    private ObjectNode buildParentToChildFacet(String fieldName) {
        final ObjectNode facet = mapper.createObjectNode();

        facet.put("type", "terms");
        facet.put("field", fieldName);

        final ObjectNode domain = mapper.createObjectNode();
        domain.put("filter", "docType:product");

        facet.set("domain", domain);
        return facet;
    }
}
