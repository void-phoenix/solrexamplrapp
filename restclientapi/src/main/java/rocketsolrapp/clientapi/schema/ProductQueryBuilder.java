package rocketsolrapp.clientapi.schema;


import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.springframework.stereotype.Service;
import rocketsolrapp.clientapi.model.RequestWithParams;
import rocketsolrapp.clientapi.model.SKU;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductQueryBuilder {

    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";
    private static final String COLOR = "color";
    private static final String SIZE = "size";
    private static final String BASE_QUERY = "{!parent which=docType:product v=$searchLegs}";
    private static final String PARENT_LEGS_QUERY = "{!dismax v=$keywords qf=$dismaxQueryFields}";

    private static final String SOLR_FILTER_QUERY_PARAM = "fq";

    private List<Field> fields;

    @PostConstruct
    private void init() {
        fields = new ArrayList<>();
        fields.add(new Field(TITLE, 2.0f, DocType.PRODUCT, FieldType.TEXT));
        fields.add(new Field(DESCRIPTION, 1.0f, DocType.PRODUCT, FieldType.TEXT));
        fields.add(new Field(COLOR, 1.0f, DocType.SKU, FieldType.TEXT));
        fields.add(new Field(SIZE, 1.0f, DocType.SKU, FieldType.TEXT));
    }

    public SolrQuery buildProductQuery(RequestWithParams requestWithParams) {
        ModifiableSolrParams params = new ModifiableSolrParams();
        SolrQuery query = new SolrQuery();
        params.add("q", BASE_QUERY);
        params.add("searchLegs", "+(" +
                //"{!lucene v=$childText} " +
                //TODO uncomment when we have childConcept query buiilder
                //"{!lucene v=$childConcept} " +
                "{!child of=docType:product v=$parentLegs})");

        params = addChildTextParam(params);
        params = addParentLegsParam(params);
        params = addChildTransformer(params);
        params = addFilters(params, requestWithParams.getFilter());
        params.add("keywords", requestWithParams.getKeywords());
        query.add(params);

        return query;

    }

    private ModifiableSolrParams addChildTextParam(ModifiableSolrParams params) {

        return params;
    }

    private ModifiableSolrParams addChildTransformer(ModifiableSolrParams params) {
        params.add("fl", "*, score, [child parentFilter=docType:product childFilter=docType:SKU limit=10]");

        return params;
    }

    private ModifiableSolrParams addFilters(ModifiableSolrParams params, List<String> filter) {
        for (String param : filter) {
            params.add(SOLR_FILTER_QUERY_PARAM, param);
        }
        return params;
    }

    private ModifiableSolrParams addParentLegsParam(ModifiableSolrParams params) {
        params.add("parentLegs", PARENT_LEGS_QUERY);
        final String dismaxQueryFields = getProductTextFields().stream().map(sku -> sku.getName() + "^" + sku.getWeight())
                .collect(Collectors.joining(" "));
        params.add("dismaxQueryFields", dismaxQueryFields);
        return params;
    }


    private List<Field> getSkuTextFields() {
        return fields.stream().filter(f -> f.getDocType().equals(DocType.SKU) &&
                f.getFieldType().equals(FieldType.TEXT))
                .collect(Collectors.toList());
    }

    private List<Field> getProductTextFields() {
        return fields.stream().filter(f -> f.getDocType().equals(DocType.PRODUCT) &&
                f.getFieldType().equals(FieldType.TEXT))
                .collect(Collectors.toList());
    }
}