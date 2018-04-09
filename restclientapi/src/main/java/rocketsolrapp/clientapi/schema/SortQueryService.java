package rocketsolrapp.clientapi.schema;


import org.apache.solr.common.params.ModifiableSolrParams;
import org.springframework.stereotype.Service;
import rocketsolrapp.clientapi.model.RequestWithParams;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SortQueryService {

    public ModifiableSolrParams addSort(ModifiableSolrParams params,
                                        RequestWithParams requestWithParams,
                                        List<Field> fields) {

        List<String> sortRawParam = requestWithParams.getSort();
        if (sortRawParam.isEmpty()) return params;
        Set<String> fieldNames = fields.stream()
                .filter(f -> f.getFieldType().equals(FieldType.SORT_FIELD))
                .map(Field::getName)
                .collect(Collectors.toSet());

       String sortQuety = sortRawParam.stream()
                .flatMap(p -> {
                    if (p.contains(",")) {
                        return Stream.of(p.split(","));
                    } else {
                        return Stream.of(p);
                    }
                })
                .map(p -> {
                    String[] fieldWithOrder = p.split(" ");
                    return new SortParam(fieldWithOrder);
                })
                .filter(e -> fieldNames.contains(e.getField()))
                .map(e -> e.getField() + " " + e.getOrder())
                .collect(Collectors.joining(","));

        params.add("sort", sortQuety);
        return params;
    }

    private class SortParam {
        private String field;
        private String order;

        SortParam(String[] parts) {
            if (parts.length == 0) {
                this.field = "score";
            } else {
                this.field = parts[0];
            }
            if (parts.length > 1 && parts[1].equals("asc")) {
                this.order = "asc";
            } else {
                this.order = "desc";
            }
        }

        public String getField() {
            return field;
        }

        public String getOrder() {
            return order;
        }

    }

}
