package rocketsolrapp.clientapi.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import rocketsolrapp.clientapi.model.RequestWithParams;

import java.util.List;

@Service
public class FacetService {

    public static final String FACET_PARAM = "json.facet";

    private final ObjectMapper mapper = new ObjectMapper();

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
