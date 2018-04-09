package rocketsolrapp.clientapi.model;

import java.util.HashMap;
import java.util.Map;

public class Facet {

    private String field;
    private Map<String, Integer> counts = new HashMap<>();

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Map<String, Integer> getCounts() {
        return counts;
    }

    public void setCounts(Map<String, Integer> counts) {
        this.counts = counts;
    }

    public void addCount(String name, Integer value) {
        this.counts.put(name, value);
    }
}
