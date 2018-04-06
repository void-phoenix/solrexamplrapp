package rocketsolrapp.clientapi.schema;


import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.StringUtils;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rocketsolrapp.clientapi.model.RequestWithParams;
import rocketsolrapp.clientapi.service.ConceptService;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductQueryBuilder {

    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";
    private static final String COLOR = "color";
    private static final String BRAND = "brand";
    private static final String SIZE = "size";
    private static final String SIZE_CINCEPT = "size_concept";
    private static final String COLOR_CONCEPT = "color_concept";
    private static final String BRAND_CONCEPT = "brand_concept";

    private static final String BASE_QUERY = "{!parent which=docType:product v=$searchLegs score=max}";
    private static final String MAX_SCORE_TO_PARENT_QUERY = "+{!maxscore v=$maxScoreLegs}";
    private static final String MAX_SCORE_CONCEPT_QUERY = "{!maxscore v=$conceptLegs}";

    private static final String SOLR_FILTER_QUERY_PARAM = "fq";
    @Autowired
    ConceptService conceptService;
    private List<Field> fields;

    private static void buildConceptCombinations(List<String> result, String temp, List<List<String>> concepts) {
        if (concepts.isEmpty()) {
            result.add(temp.trim());
            return;
        }
        final List<String> terms = concepts.get(0);
        final List<List<String>> rest = new ArrayList<List<String>>(concepts);
        rest.remove(terms);
        for (String term : terms) {
            buildConceptCombinations(result, temp + " " + term, rest);
        }
    }

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
        fields.add(new Field(BRAND, 1.0f, DocType.PRODUCT, FieldType.TEXT));
    }

    public SolrQuery buildProductQuery(RequestWithParams requestWithParams) throws Exception {
        ModifiableSolrParams params = new ModifiableSolrParams();

        SolrQuery query = new SolrQuery();
        params.add("q", BASE_QUERY);
        params = buildSearchLegs(requestWithParams, params);
        params = addChildTransformer(params);
        params = addFilters(params, requestWithParams.getFilter());
        query.add(params);

        return query;

    }

    private ModifiableSolrParams buildSearchLegs(RequestWithParams requestWithParams, ModifiableSolrParams params) throws Exception {

        final NamedList conceptResponce = conceptService.getConcepts(requestWithParams.getKeywords());

        StringBuilder searchLegs = new StringBuilder("+(");

        searchLegs = addConceptsToSearchLegs(requestWithParams, params, searchLegs, conceptResponce);
        searchLegs = addParentTextLegs(params, searchLegs, requestWithParams.getKeywords(), conceptResponce);

        searchLegs.append(")");
        params.add("searchLegs", searchLegs.toString());

        //TODO params.add("keywords", requestWithParams.getKeywords());
        return params;
    }

    private StringBuilder addParentTextLegs(ModifiableSolrParams params, StringBuilder searchLegs,
                                            String keywords, NamedList conceptResponce) {
        final String disMaxQueryFields = getDisMaxQueryFields(params);
        if (StringUtils.isEmpty(disMaxQueryFields)) return searchLegs;
        searchLegs.append(MAX_SCORE_TO_PARENT_QUERY);
        params.add("dismaxQueryFields", disMaxQueryFields);

        buildMaxScoreLegs(keywords, params, conceptResponce);

        return searchLegs;
    }

    private StringBuilder addConceptsToSearchLegs(RequestWithParams requestWithParams, ModifiableSolrParams params,
                                                  StringBuilder searchLegs, NamedList conceptResponce) throws Exception {
        final List<List<String>> conceptsByField = getConceptParam(requestWithParams, conceptResponce);
        if (conceptsByField.isEmpty()) return searchLegs;
        searchLegs.append(MAX_SCORE_CONCEPT_QUERY);
        searchLegs.append(" ");

        List<String> conceptCombinations = new ArrayList<>();
        buildConceptCombinations(conceptCombinations, "", conceptsByField);

        final StringBuilder conceptLegs = new StringBuilder();
        for (int i = 0; i < conceptCombinations.size(); i++) {
            conceptLegs.append("{!lucene v=$conceptCombination");
            conceptLegs.append(i);
            conceptLegs.append("} ");
            params.add("conceptCombination" + i, conceptCombinations.get(i));
        }
        conceptCombinations.forEach(c -> {

        });

        params.add("conceptLegs", conceptLegs.toString());
        return searchLegs;
    }

    private List<List<String>> getConceptParam(RequestWithParams requestWithParams, NamedList conceptResponce) throws Exception {

        final List<String> conceptIds = new ArrayList<>();

        final SolrDocumentList response = (SolrDocumentList) conceptResponce.get("response");
        final List<List<String>> concepts = new ArrayList<>();
        response.stream()
                .filter(c -> c.getFieldValue("type").equals(ConceptService.CONCEPT_TYPE))
                .forEach(c -> {
                    final String field = (String) c.get("field");
                    final String boost = getConceptBoost(field);
                    if (boost == null) return;
                    conceptIds.add((String) c.get("id"));
                    final List<String> terns = new ArrayList<>();
                    concepts.add(terns);
                    final List<String> synonyms = (List<String>) c.get("synonyms");

                    synonyms.forEach(s -> terns.add(field + ":" + s + boost));
                });

        final List<NamedList> tags = (List<NamedList>) conceptResponce.get("tags");
        for (NamedList tag : tags) {
            final String mathcedText = (String) tag.get("matchText");
            boolean idInList = false;
            final List<String> ids = (List<String>) tag.get("ids");
            for (String id : conceptIds) {
                if (ids.contains(id)) {
                    idInList = true;
                    break;
                }
            }
            if (idInList) {
                requestWithParams.setKeywords(requestWithParams.getKeywords().replaceAll(mathcedText, ""));
            }
        }
        requestWithParams.setKeywords(requestWithParams.getKeywords().trim());
        return concepts;
    }

    private ModifiableSolrParams addChildTransformer(ModifiableSolrParams params) {
        params.add("fl", "*, score, [child parentFilter=docType:product childFilter=docType:SKU limit=10]");

        return params;
    }

    private ModifiableSolrParams addFilters(ModifiableSolrParams params, List<String> filters) {

        final Map<String, List<String>> groupedByField = new HashMap<>();
        for (String filter : filters) {
            final String key = filter.split(":")[0];
            groupedByField.putIfAbsent(key, new ArrayList<>());
            groupedByField.get(key).add(filter);
        }

        for (Map.Entry<String, List<String>> entry : groupedByField.entrySet()){
            if (entry.getValue().size() == 1) {
                final String queryParam = buildFilterQuery(entry.getValue().get(0));
                params.add(SOLR_FILTER_QUERY_PARAM, queryParam);
            } else {
                StringBuilder filterQuery = new StringBuilder("=(");
                for (String filter : entry.getValue()) {
                    filterQuery.append(buildFilterQuery(filter));
                    filterQuery.append(" ");
                }
                filterQuery.append(")");
                params.add(SOLR_FILTER_QUERY_PARAM, filterQuery.toString());
            }
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

    public List<String> getConceptFields() {
        return fields.stream()
                .filter(f -> f.getFieldType().equals(FieldType.CONCEPT))
                .map(Field::getName)
                .collect(Collectors.toList());
    }

    public SolrQuery buildConceptsRequestForField(String field) {
        SolrQuery query = new SolrQuery("*:*");
        query.add("rows", "0");
        query.add("facet", "on");
        query.add("facet.field", field);
        return query;
    }

    private Field findByName(String name) {
        List<Field> found = fields.stream().filter(f -> f.getName().equals(name)).collect(Collectors.toList());
        if (found.size() < 1) return null;
        return found.get(0);
    }

    public String getConceptBoost(String conceptName) {
        final List<String> weights = fields.stream()
                .filter(f -> f.getName().equals(conceptName) && f.getDocType().equals(DocType.SKU))
                .map(f -> "^" + f.getWeight())
                .collect(Collectors.toList());
        if (weights.isEmpty()) return null;
        else return weights.get(0);
    }

    private String buildFilterQuery(String queryTerm) {
        final String[] fieldWithTerm = queryTerm.split(":");
        if (fieldWithTerm.length != 2) return null;
        final Field field = findByName(fieldWithTerm[0]);
        if (field == null) return null;
        if (field.getDocType().equals(DocType.PRODUCT)) {
            return queryTerm;
        } else {
            return "{!parent which=docType:product v=" + queryTerm + "}";
        }
    }

    private void buildMaxScoreLegs(String keywords, ModifiableSolrParams params, NamedList conceptResponce) {

        List<String> phrases = buildSynonymPhrases(keywords, conceptResponce);

        StringBuilder maxScoreLegs = new StringBuilder();
        for (int i = 0; i < phrases.size(); i++) {
            final String phrase = phrases.get(i);
            maxScoreLegs.append("{!child of=docType:product v=$dismax");
            maxScoreLegs.append(i);
            maxScoreLegs.append(" score=sum} ");
            params.add("dismax" + i, "{!dismax v=$keywords" + i + " qf=$dismaxQueryFields mm=100%}  ");
            params.add("keywords" + i, phrase);
        }
        params.add("maxScoreLegs", maxScoreLegs.toString());
    }

    private List<String> buildSynonymPhrases(String keywords, NamedList conceptResponce) {

        final SolrDocumentList response = (SolrDocumentList) conceptResponce.get("response");
        final Map<String, List<String>> concepts = new HashMap<>();
        response.stream()
                .filter(c -> c.getFieldValue("type").equals(ConceptService.SYNONYM_TYPE))
                .forEach(c -> {
                    final String from = (String) c.get("searchTerms");
                    final List<String> synonyms = (List<String>) c.get("synonyms");
                    concepts.putIfAbsent(from, synonyms);
                });

        final List<String> result = new ArrayList<>();

        buildPhrases(result, keywords, concepts);
        return result;
    }

    private static void buildPhrases(List<String> result, String keywords, Map<String, List<String>> concepts) {

        if (concepts.isEmpty()) {
            result.add(keywords);
            return;
        }

        final String term = (String) concepts.keySet().toArray()[0];
        final List<String> synonyms = concepts.get(term);

        final Map<String, List<String>> rest = new HashMap<>(concepts);
        rest.remove(term);
        for (String synonym : synonyms) {
            buildPhrases(
                    result,
                    keywords.replaceAll(term, synonym),
                    rest);
        }
    }
}