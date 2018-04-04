package rocketsolrapp.clientapi.schema;


import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.StringUtils;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rocketsolrapp.clientapi.model.RequestWithParams;
import rocketsolrapp.clientapi.service.ConceptService;

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
    private static final String SIZE_CINCEPT = "size_concept";
    private static final String COLOR_CONCEPT = "color_concept";
    private static final String BRAND_CONCEPT = "brand_concept";
    private static final String BASE_QUERY = "{!parent which=docType:product v=$searchLegs score=max}";
    private static final String PARENT_LEGS_QUERY = "{!dismax v=$keywords qf=$dismaxQueryFields}";

    private static final String SOLR_FILTER_QUERY_PARAM = "fq";

    private List<Field> fields;

    @Autowired
    ConceptService conceptService;

    @PostConstruct
    private void init() {
        fields = new ArrayList<>();
        fields.add(new Field(TITLE, 2.0f, DocType.PRODUCT, FieldType.TEXT));
        fields.add(new Field(DESCRIPTION, 1.0f, DocType.PRODUCT, FieldType.TEXT));
        fields.add(new Field(COLOR, 1.0f, DocType.SKU, FieldType.TEXT));
        fields.add(new Field(SIZE, 1.0f, DocType.SKU, FieldType.TEXT));
        fields.add(new Field(SIZE_CINCEPT, 2.0f, DocType.SKU, FieldType.CONCEPT));
        fields.add(new Field(COLOR_CONCEPT, 3.0f, DocType.SKU, FieldType.CONCEPT));
        fields.add(new Field(BRAND_CONCEPT, 3.0f, DocType.PRODUCT, FieldType.CONCEPT));
    }

    public SolrQuery buildProductQuery(RequestWithParams requestWithParams) throws Exception{
        ModifiableSolrParams params = new ModifiableSolrParams();
        SolrQuery query = new SolrQuery();
        params.add("q", BASE_QUERY);
        final StringBuilder searchLegs = new StringBuilder("+(");

        final String conceptParam = getConceptParam(requestWithParams);
        if (!StringUtils.isEmpty(conceptParam)) {
            searchLegs.append("{!lucene v=$childConcept} ");
            params.add("childConcept", conceptParam);
        }

        final String disMaxQueryFields = getDisMaxQueryFields(params);
        if (!StringUtils.isEmpty(disMaxQueryFields)){
            searchLegs.append("+{!child of=docType:product v=$parentLegs score=sum} ");
            params.add("parentLegs", PARENT_LEGS_QUERY);
            params.add("dismaxQueryFields", disMaxQueryFields);
        }

        searchLegs.append(")");
        params.add("searchLegs", searchLegs.toString());

        params = addChildTransformer(params);
        params = addFilters(params, requestWithParams.getFilter());
        params.add("keywords", requestWithParams.getKeywords());
        query.add(params);

        return query;

    }

    private String getConceptParam(RequestWithParams requestWithParams) throws Exception{

        final SolrDocumentList concepts = conceptService.getConcepts(requestWithParams.getKeywords());

        final String conceptQuery = concepts.stream().map(c -> {
            final String field = (String) c.get("field");
            final String boost = getConceptBoost(field);
            if (boost == null) return "";
            else return field + ":" + c.get("searchTerms") + boost;
        }).collect(Collectors.joining(" "));
        return conceptQuery;
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

    private String getDisMaxQueryFields(ModifiableSolrParams params) {
        return getProductTextFields().stream().map(sku -> sku.getName() + "^" + sku.getWeight())
                .collect(Collectors.joining(" "));
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

    public List<String> getConceptFields(){
        return fields.stream()
                .filter(f -> f.getFieldType().equals(FieldType.CONCEPT))
                .map(Field::getName)
                .collect(Collectors.toList());
    }

    public SolrQuery buildConceptsRequestForField(String field){
        SolrQuery query = new SolrQuery("*:*");
        query.add("rows", "0");
        query.add("facet", "on");
        query.add("facet.field", field);
        return query;
    }

    public String getConceptBoost(String conceptName) {
        final List<String> weights = fields.stream()
                .filter(f -> f.getName().equals(conceptName) && f.getDocType().equals(DocType.SKU))
                .map(f -> "^" + f.getWeight())
                .collect(Collectors.toList());
        if (weights.isEmpty()) return null;
        else return weights.get(0);
    }
}