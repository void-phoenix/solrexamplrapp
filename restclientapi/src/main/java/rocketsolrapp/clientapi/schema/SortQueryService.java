package rocketsolrapp.clientapi.schema;


import org.apache.solr.common.params.ModifiableSolrParams;
import org.springframework.stereotype.Service;
import rocketsolrapp.clientapi.model.RequestWithParams;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SortQueryService {

    private Map<Integer, String> sortOptions;

    @PostConstruct
    private void init(){
        sortOptions = new HashMap<>();
        sortOptions.put(0, "score desc");
        sortOptions.put(1, "price asc");
        sortOptions.put(2, "price desc");
        sortOptions.put(3, "rating asc");
        sortOptions.put(4, "rating desc");
    }

    public ModifiableSolrParams addSort(ModifiableSolrParams params,
                                        RequestWithParams requestWithParams) {

        final Integer sortKey = requestWithParams.getSortKey();
        if (sortOptions.containsKey(sortKey)){
            params.add("sort", sortOptions.get(sortKey));
        }
        return params;
    }

}
