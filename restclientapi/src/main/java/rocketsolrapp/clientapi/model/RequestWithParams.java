package rocketsolrapp.clientapi.model;

import java.util.ArrayList;
import java.util.List;

public class RequestWithParams {

    private String keywords;

    private List<String> filter = new ArrayList<>();

    private List<String> sort = new ArrayList<>();

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public List<String> getFilter() {
        return filter;
    }

    public void setFilter(List<String> filter) {
        this.filter = filter;
    }

    public List<String> getSort() {
        return sort;
    }

    public void setSort(List<String> sort) {
        this.sort = sort;
    }
}
