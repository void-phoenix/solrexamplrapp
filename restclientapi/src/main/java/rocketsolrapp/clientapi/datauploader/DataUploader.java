package rocketsolrapp.clientapi.datauploader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rocketsolrapp.clientapi.model.product.Concept;
import rocketsolrapp.clientapi.model.product.Product;
import rocketsolrapp.clientapi.model.rule.Action;
import rocketsolrapp.clientapi.schema.ProductQueryBuilder;
import rocketsolrapp.clientapi.service.ConceptService;
import rocketsolrapp.clientapi.service.ProductService;
import rocketsolrapp.clientapi.service.RuleService;
import rocketsolrapp.clientapi.service.SolrRequester;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
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

    @Autowired
    ConceptService conceptService;

    @Autowired
    RuleService ruleService;

    private ExecutorService executorService;


    @PostConstruct
    private void init() {
        executorService = Executors.newFixedThreadPool(
                Math.max(Runtime.getRuntime().availableProcessors() / 2, 1)
        );
    }

    public void reloadConcepts() throws Exception {
        clearConcepts();
        loadSynonyms();
        for (String conceptField : productQueryBuilder.getConceptFields()) {
            executorService.submit(new UpdateConceptTask(conceptField));
        }
    }

    public void reloadRules() throws Exception {
        ruleService.clear();
        loadRules();
    }

    private void clearConcepts() throws Exception {
        final UpdateRequest request = new UpdateRequest();
        request.deleteByQuery("*:*");
        try {
            solrRequester.sendSolrRequest("concepts", request);
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
        }
    }

    private void loadRules() throws Exception {
        final InputStream content = getResourceByName("rules.json");
        final ObjectMapper objectMapper = new ObjectMapper();
        final TypeFactory typeFactory = objectMapper.getTypeFactory();
        final List<Action> actions = objectMapper.readValue(content, typeFactory.constructCollectionType(List.class, Action.class));
        ruleService.add(actions);
    }

    private void loadSynonyms() throws Exception {
        final InputStream content = getResourceByName("synonyms.json");
        final ObjectMapper objectMapper = new ObjectMapper();
        final TypeFactory typeFactory = objectMapper.getTypeFactory();
        final List<Concept> synonyms = objectMapper.readValue(content, typeFactory.constructCollectionType(List.class, Concept.class));
        conceptService.add(synonyms);
    }

    public void reloadProducts() throws IOException, SolrServerException {
        productService.clear();
        Map<String, Set<String>> inventoryDict = loadInventoryDict();
        final InputStream content = getResourceByName("products.json");
        final ObjectMapper objectMapper = new ObjectMapper();
        final TypeFactory typeFactory = objectMapper.getTypeFactory();
        final List<Product> products = objectMapper.readValue(content, typeFactory.constructCollectionType(List.class, Product.class));
        productService.add(products, inventoryDict);
    }

    private InputStream getResourceByName(String name) throws IOException {
        final String resoutcePath = "embeddedsolr" + File.separator +
                "src" + File.separator +
                "main" + File.separator +
                "resources" + File.separator +
                name;

        return Files.newInputStream(Paths.get(resoutcePath));
    }

    private Map<String, Set<String>> loadInventoryDict() throws IOException {
        final InputStream inventory = getResourceByName("inventory.csv");
        final Map<String, Set<String>> result = new HashMap<>();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inventory));

        reader.lines().forEach(line -> {
            final String[] kv = line.split(",");
            if (kv.length != 2) return;
            result.putIfAbsent(kv[0], new HashSet<>());
            result.get(kv[0]).add(kv[1]);
        });
        return result;
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
                final List<String> synonims = getSynonyms(conceptString);
                final SolrInputDocument conceptDocument = new SolrInputDocument();
                conceptDocument.setField("field", conceptField);
                conceptDocument.setField("searchTerms", conceptString);
                conceptDocument.setField("type", CONCEPT_TYPE);
                conceptDocument.setField("synonyms", synonims);
                final UpdateRequest request = new UpdateRequest();
                request.add(conceptDocument);
                try {
                    solrRequester.sendSolrRequest("concepts", request);
                } catch (Exception ex) {
                    LOG.error(ex.getMessage());
                }
            });
        }

        private List<String> getSynonyms(String concept) {
            final List<String> result = new ArrayList<>();
            try {
                NamedList synonymsResponce = conceptService.getSynonyms(concept);
                final SolrDocumentList response = (SolrDocumentList) synonymsResponce.get("response");
                if (response.isEmpty()) {
                    result.add(concept);
                } else {
                    final SolrDocument document = response.get(0);
                    final List<String> synonyms = (List<String>) document.get("synonyms");
                    result.addAll(synonyms);
                }
            } catch (Exception ex) {
                LOG.error(ex.getMessage());
                if (result.isEmpty()) {
                    result.add(concept);
                }
            }
            return result;
        }
    }
}
