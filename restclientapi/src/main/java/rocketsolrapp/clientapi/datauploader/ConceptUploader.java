package rocketsolrapp.clientapi.datauploader;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rocketsolrapp.clientapi.schema.ProductQueryBuilder;
import rocketsolrapp.clientapi.service.SolrRequester;
import rocketsolrapp.solr.factory.SolrClientFactory;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


@Service
public class ConceptUploader {

    private static final Logger LOG = LoggerFactory.getLogger(ConceptUploader.class);

    @Autowired
    ProductQueryBuilder productQueryBuilder;

    @Autowired
    SolrRequester solrRequester;

    private ExecutorService executorService;

    @PostConstruct
    private void init() {
        executorService = Executors.newFixedThreadPool(
                Math.max(Runtime.getRuntime().availableProcessors() / 2, 1)
        );
    }

    public void uploadConcepts() {
        clearConcepts();
        for (String conceptField : productQueryBuilder.getConceptFields()) {
            executorService.submit(new UpdateConceptTask(conceptField));
        }
    }

    class UpdateConceptTask implements Runnable {

        private final String conceptField;

        UpdateConceptTask(String conceptField) {
            this.conceptField = conceptField;
        }

        @Override
        public void run() {
            final List<String> concepts = queryForConcepts();
            updateConceptCore(concepts);
        }

        private List<String> queryForConcepts() {
            final SolrQuery query = productQueryBuilder.buildConceptsRequestForField(conceptField);
            final QueryResponse response = solrRequester.executeQuery("products", query);
            final List<FacetField> facetFieldList = response.getFacetFields();
            if (facetFieldList.size() != 1) {
                LOG.error("Haven't found any facets for field: " + conceptField);
                return Collections.EMPTY_LIST;
            }

            final FacetField facet = facetFieldList.get(0);
            return facet.getValues().stream()
                    .map(FacetField.Count::getName)
                    .collect(Collectors.toList());
        }

        private void updateConceptCore(List<String> concepts) {
            concepts.forEach(conceptString -> {
                final SolrInputDocument conceptDocument = new SolrInputDocument();
                conceptDocument.setField("field", conceptField);
                conceptDocument.setField("searchTerms", conceptString);
                final UpdateRequest request = new UpdateRequest();
                request.add(conceptDocument);try {
                    solrRequester.sendSolrRequest("concepts", request);
                } catch (Exception ex) {
                    LOG.error(ex.getMessage());
                }
            });
        }
    }

    private void clearConcepts(){
        final UpdateRequest request = new UpdateRequest();
        request.deleteByQuery("*:*");
        try {
            solrRequester.sendSolrRequest("concepts", request);
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
        }

    }
}
