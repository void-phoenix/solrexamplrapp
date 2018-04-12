package rocketsolrapp.clientapi.schema;


import org.apache.solr.common.params.ModifiableSolrParams;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilterService {

    private static final String SOLR_FILTER_QUERY_PARAM = "fq";

    public ModifiableSolrParams addFilters(ModifiableSolrParams params, List<String> filters, List<Field> fields) {

        final Map<String, List<String>> groupedByField = groupByFields(filters);

        long storeFiltersCount = groupedByField.keySet().stream().filter( k -> k.startsWith("store_")).count();
        if (storeFiltersCount == 0) {
            groupedByField.put("store_0", Collections.singletonList("store_0:1"));
        }

        for (Map.Entry<String, List<String>> entry : groupedByField.entrySet()) {
            if (entry.getValue().size() == 1) {
                buildSingleTermFilter(params, fields, entry);
            } else {
                buildMultyTermFilter(params, fields, entry);
            }
        }
        return params;
    }

    private void buildMultyTermFilter(ModifiableSolrParams params, List<Field> fields, Map.Entry<String, List<String>> entry) {
        String tag = entry.getKey().toUpperCase();
        if (tag.startsWith("STORE_")){
            tag = "STORE";
        }
        params.add(SOLR_FILTER_QUERY_PARAM, "{!tag=" + tag + " v=$" + entry.getKey() + "_filterValue }");
        StringBuilder filterQuery = new StringBuilder("=(");
        for (String filter : entry.getValue()) {
            filterQuery.append(buildFilterQuery(filter, fields));
            filterQuery.append(" ");
        }
        filterQuery.append(")");
        params.add(entry.getKey() + "_filterValue", filterQuery.toString());
    }

    private void buildSingleTermFilter(ModifiableSolrParams params, List<Field> fields, Map.Entry<String, List<String>> entry) {
        String tag = entry.getKey().toUpperCase();
        if (tag.startsWith("STORE_")){
            tag = "STORE";
        }
        params.add(SOLR_FILTER_QUERY_PARAM, "{!tag=" + tag + " v=$" + entry.getKey() + "_filterValue }");
        final String queryParam = buildFilterQuery(entry.getValue().get(0), fields);
        params.add(entry.getKey() + "_filterValue", queryParam);
    }

    private Map<String, List<String>> groupByFields(List<String> filters) {
        final Map<String, List<String>> groupedByField = new HashMap<>();
        for (String filter : filters) {
            final String key = filter.split(":")[0];
            groupedByField.putIfAbsent(key, new ArrayList<>());
            groupedByField.get(key).add(filter);
        }
        return groupedByField;
    }

    private String buildFilterQuery(String queryTerm, List<Field> fields) {
        final String[] fieldWithTerm = queryTerm.split(":");
        if (fieldWithTerm.length != 2) return null;
        final Field field = findByName(fieldWithTerm[0], fields);
        if (field == null) return null;
        if (field.getDocType().equals(DocType.PRODUCT)) {
            return queryTerm;
        } else {
            return "{!parent which=docType:product v=" + queryTerm + "}";
        }
    }

    private Field findByName(String name, List<Field> fields) {

        if (name.startsWith("store_")) return new Field(name, 1f, DocType.SKU, FieldType.TEXT);

        List<Field> found = fields.stream().filter(f -> f.getName().equals(name)).collect(Collectors.toList());
        if (found.size() < 1) return null;
        return found.get(0);
    }

}
