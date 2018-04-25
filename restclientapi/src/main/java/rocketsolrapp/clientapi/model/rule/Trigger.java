package rocketsolrapp.clientapi.model.rule;

import java.util.ArrayList;
import java.util.List;

public class Trigger {

    private String id;
    private String matchmode;
    private String searchTerms;
    private boolean exactLocation;
    private List<String> filterId = new ArrayList<>();
    private String docType;

    public String getMatchmode() {
        return matchmode;
    }

    public void setMatchmode(String matchmode) {
        this.matchmode = matchmode;
    }

    public String getSearchTerms() {
        return searchTerms;
    }

    public void setSearchTerms(String searchTerms) {
        this.searchTerms = searchTerms;
    }

    public boolean isExactLocation() {
        return exactLocation;
    }

    public void setExactLocation(boolean exactLocation) {
        this.exactLocation = exactLocation;
    }

    public List<String> getFilterId() {
        return filterId;
    }

    public void setFilterId(List<String> filterId) {
        this.filterId = filterId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }
}


