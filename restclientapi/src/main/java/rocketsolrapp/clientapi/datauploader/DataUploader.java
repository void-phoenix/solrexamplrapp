package rocketsolrapp.clientapi.datauploader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rocketsolrapp.clientapi.model.Product;
import rocketsolrapp.clientapi.model.SolrTaggerRequest;
import rocketsolrapp.clientapi.schema.ProductQueryBuilder;
import rocketsolrapp.clientapi.service.ProductService;
import rocketsolrapp.clientapi.service.SolrRequester;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static rocketsolrapp.clientapi.service.ConceptService.CONCEPT_TYPE;


@Service
public class DataUploader {

    private static final Logger LOG = LoggerFactory.getLogger(DataUploader.class);


    @Autowired
    ProductQueryBuilder productQueryBuilder;

    @Autowired
    SolrRequester solrRequester;

    @Autowired
    ProductService productService;

    private ExecutorService executorService;

    @PostConstruct
    private void init() {
        executorService = Executors.newFixedThreadPool(
                Math.max(Runtime.getRuntime().availableProcessors() / 2, 1)
        );
    }

    public void reloadConcepts() {
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
                conceptDocument.setField("type", CONCEPT_TYPE);
                conceptDocument.setField("synonyms", conceptString);
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

    public void reloadProducts() throws IOException, SolrServerException {
        productService.clear();
        final byte[] content = Files.readAllBytes(Paths.get("embeddedsolr" + File.separator
                + "src" + File.separator +
                "main" + File.separator +
                "resources" + File.separator +
                "products.json"
        ));
        final ObjectMapper objectMapper = new ObjectMapper();
        final TypeFactory typeFactory = objectMapper.getTypeFactory();
        final List<Product> products = objectMapper.readValue(content, typeFactory.constructCollectionType(List.class, Product.class));
        productService.add(products);
    }
}
