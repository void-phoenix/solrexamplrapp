package rocketsolrapp.clientapi.spelling;

import com.google.common.collect.Comparators;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.util.plugin.SolrCoreAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ExcludingSpellingComponent extends SearchComponent implements SolrCoreAware {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private ExcludeProcessor processor;
    private NamedList initParams;

    @Override
    public void init(NamedList args) {
        super.init(args);
        this.initParams = args;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void prepare(ResponseBuilder rb) throws IOException {

    }

    @Override
    public void inform(SolrCore core) {
        if (initParams != null) {
            addExculuder(core, initParams);
        }
    }

    public void process(ResponseBuilder rb) {
        //TODO remove terms from request if there are in this index
        NamedList values = rb.rsp.getValues();
        NamedList spellcheck = (NamedList) values.get("spellcheck");
        if (spellcheck == null) return;

        final List<String> suggestions = extractSuggestion(spellcheck);
        try {
            final Set<String> toExclude = processor.findProhibitedTerms(suggestions);
            removeTerms(toExclude, spellcheck);
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
        }
    }

    private void addExculuder(SolrCore core, NamedList excluderParams) {
        processor = new ExcludeProcessor();
        processor.init(excluderParams, core);
    }

    @Override
    public String getDescription() {
        return "Simple excluder for spell suggestions";
    }

    private List<String> extractSuggestion(NamedList spellcheck) {

        List<String> allSuggestions = new ArrayList<>();

        NamedList suggestions = (NamedList) spellcheck.get("suggestions");
        //TODO do we need this?
        NamedList collations = (NamedList) spellcheck.get("collations");

        for (int i = 0; i < suggestions.size(); i++) {
            //TODO do we need this?
            final String name = suggestions.getName(i);
            final SimpleOrderedMap value = (SimpleOrderedMap) suggestions.getVal(i);
            final List<SimpleOrderedMap> localSuggestions = (List<SimpleOrderedMap>) value.get("suggestion");
            for (SimpleOrderedMap localSuggestion : localSuggestions) {
                final String word = (String) localSuggestion.get("word");
                allSuggestions.add(word);
            }
        }
        return allSuggestions;
    }

    private void removeTerms(Set<String> terms, NamedList spellCheck){
        removeTermFromSuggestions(terms, (NamedList) spellCheck.get("suggestions"));
        removeTermFromCollations(terms, (NamedList) spellCheck.get("collations"));
    }

    private void removeTermFromSuggestions(Set<String> terms, NamedList suggestions) {
        for (int i = 0; i < suggestions.size(); i++) {
            SimpleOrderedMap values = (SimpleOrderedMap) suggestions.getVal(i);
            final List<SimpleOrderedMap> localSuggestions = (List<SimpleOrderedMap>) values.get("suggestion");
            List<SimpleOrderedMap> toRemove = new ArrayList<>();
            for (int j = 0; j < localSuggestions.size(); j++) {
                SimpleOrderedMap localSuggestion = localSuggestions.get(j);
                final String word = (String) localSuggestion.get("word");
                if (terms.contains(word)) {
                    toRemove.add(localSuggestion);
                }
            }
            for (SimpleOrderedMap removeCandidate : toRemove) {
                localSuggestions.remove(removeCandidate);
            }
            Integer numFoumd = (Integer) values.get("numFound");
            numFoumd = numFoumd - toRemove.size();
            values.setVal(values.indexOf("numFound", 0), numFoumd);
        }
    }

    private void removeTermFromCollations(Set<String> terms, NamedList collations) {
        final List<Integer> toRemove = new ArrayList<>();
        for (int i = 0; i < collations.size(); i++) {
            if (!collations.getName(i).equals("collation")) continue;
            NamedList collation = (NamedList) collations.getVal(i);
            NamedList corrections = (NamedList) collation.get("misspellingsAndCorrections");
            for (int j = 0; j < corrections.size(); j++ ) {
                if (terms.contains( (String) corrections.getVal(j))){
                    toRemove.add(i);
                }
            }
        }
        for (int i = toRemove.size() - 1; i >=0; i--) {
            collations.remove(toRemove.get(i));
        }
    }
}






