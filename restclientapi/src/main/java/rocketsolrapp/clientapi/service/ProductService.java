package rocketsolrapp.clientapi.service;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rocketsolrapp.clientapi.model.*;
import rocketsolrapp.clientapi.schema.FacetQueryService;
import rocketsolrapp.clientapi.schema.ProductQueryBuilder;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductService.class);

    public static final String CORE_NAME = "products";

    @Autowired
    SolrRequester solr;

    @Autowired
    ProductQueryBuilder productRequestbuilder;

    @Autowired
    FacetQueryService facetService;

    public SearchResponse query(RequestWithParams request) throws Exception {
        final SearchResponse searchResponse = new SearchResponse();
        final SolrQuery query = productRequestbuilder.buildProductQuery(request);

        final QueryResponse response = solr.executeQuery(CORE_NAME, query);

        final List<Product> result = extractProducts(response);
        searchResponse.setProducts(result);
        final List<Facet> facetResult = facetService.extractFacets(response);

        searchResponse.setFacets(facetResult);
        return searchResponse;
    }

    public void add(Product product) throws SolrServerException, IOException {
        final UpdateRequest request = new UpdateRequest();
        request.add(convertProductToSolrFormat(product), false);
        solr.sendSolrRequest(CORE_NAME, request);
    }

    public void updateInventory(String id, String value) throws SolrServerException, IOException {
        final SolrInputDocument document = new SolrInputDocument();
        document.addField("id", id);
        final Map<String, Object> modifiers = new HashMap<>();
        final String[] kv = value.split(":");
        if (kv.length != 2 ) return;
        if (!kv[0].startsWith("store_")) return;
        try {
            Integer fieldValue = Integer.valueOf(kv[1]);
            modifiers.put("set", fieldValue);
            document.addField(kv[0], modifiers);
            UpdateRequest request = new UpdateRequest();
            request.add(document);
            solr.sendSolrRequest(CORE_NAME, request);
        } catch (NumberFormatException nfe) {
            LOG.error(nfe.getMessage());
        }
    }

    public void add(List<Product> products, Map<String, Set<String>> inventory) throws SolrServerException, IOException {
        final UpdateRequest request = new UpdateRequest();
        List<SolrInputDocument> inputDocuments = products.stream().map(this::convertProductToSolrFormat).collect(Collectors.toList());
        addInventory(inventory, inputDocuments);
        request.add(inputDocuments);
        solr.sendSolrRequest(CORE_NAME, request);
    }

    private void addInventory(Map<String, Set<String>> inventory, List<SolrInputDocument> inputDocuments) {
        inputDocuments.forEach(product -> {
            product.getChildDocuments().forEach(sku -> {
                final String id = (String) sku.getFieldValue("id");
                if (inventory.containsKey(id)){
                    inventory.get(id).forEach( v -> {
                        sku.addField(v, 1);
                    });
                }
            });
        });
    }

    public void add(List<Product> products) throws SolrServerException, IOException {
        final UpdateRequest request = new UpdateRequest();
        List<SolrInputDocument> inputDocuments = products.stream().map(this::convertProductToSolrFormat).collect(Collectors.toList());
        request.add(inputDocuments);
        solr.sendSolrRequest(CORE_NAME, request);
    }

    public void clear() throws SolrServerException, IOException {
        final UpdateRequest request = new UpdateRequest();
        request.deleteByQuery("*:*");
        solr.sendSolrRequest(CORE_NAME, request);

    }

    public Product update(Product product) throws SolrServerException, IOException {
        final UpdateRequest request = new UpdateRequest();
        request.add(convertProductToSolrFormat(product), true);
        solr.sendSolrRequest(CORE_NAME, request);
        return product;
    }

    public void delete(Product product) throws SolrServerException, IOException {
        final UpdateRequest request = new UpdateRequest();
        request.deleteByQuery("_root_:" + product.getId());
        solr.sendSolrRequest(CORE_NAME, request);
    }


    private Map<String, List<SolrDocument>> groupByRoot(SolrDocumentList documents) {
        Map<String, List<SolrDocument>> result = new HashMap<>();
        for (SolrDocument document : documents) {
            final String rootId = (String) document.getFieldValue("_root_");
            if (result.containsKey(rootId)) {
                result.get(rootId).add(document);
            } else {
                result.put(rootId, new ArrayList<>(Collections.singletonList(document)));
            }
        }
        return result;
    }

    private SolrInputDocument convertProductToSolrFormat(Product product) {
        SolrInputDocument productDocument = new SolrInputDocument();
        productDocument.setField("id", product.getId());
        productDocument.setField("brand", product.getBrand());
        productDocument.setField("price", product.getPrice());
        productDocument.setField("docType", "product");
        productDocument.setField("title", product.getTitle());
        productDocument.setField("description", product.getDescription());
        productDocument.setField("department", product.getDepartment());
        productDocument.setField("rating", product.getRating());
        for (SKU sku : product.getSkus()) {
            SolrInputDocument skuDocument = convertSkuToSolrFormat(sku);

            productDocument.addChildDocument(skuDocument);
        }
        return productDocument;
    }

    private SolrInputDocument convertSkuToSolrFormat(SKU sku) {
        SolrInputDocument skuDocument = new SolrInputDocument();

        skuDocument.setField("docType", "SKU");
        skuDocument.setField("id", sku.getId());
        skuDocument.setField("color", sku.getColor());
        skuDocument.setField("size", sku.getSize());
        skuDocument.setField("store_0", sku.isStore_0());
        return skuDocument;
    }

    private List<Product> extractProducts(QueryResponse response) {
        final List<Product> result = new ArrayList<>();
        for (SolrDocument solrDocument : response.getResults()) {
            final Product product = new Product();
            product.setId((String) solrDocument.getFieldValue("id"));
            product.setBrand((String) solrDocument.getFieldValue("brand"));
            product.setDepartment((String) solrDocument.getFieldValue("department"));
            product.setDescription((String) solrDocument.getFieldValue("description"));
            product.setPrice((double) solrDocument.getFieldValue("price"));
            product.setTitle((String) solrDocument.getFieldValue("title"));
            product.setRating((double) solrDocument.getFieldValue("rating"));
            if (solrDocument.containsKey("score")) {
                product.setScore(String.valueOf((float) solrDocument.getFieldValue("score")));
            }

            for (SolrDocument skuDocument : solrDocument.getChildDocuments()) {
                final SKU sku = extractSku(skuDocument);
                product.addSKU(sku);
            }
            result.add(product);
        }

        return result;
    }

    private SKU extractSku(SolrDocument skuDocument) {
        final SKU sku = new SKU();
        sku.setId((String) skuDocument.getFieldValue("id"));
        sku.setColor((String) skuDocument.getFieldValue("color"));
        sku.setSize((String) skuDocument.getFieldValue("size"));
        return sku;
    }
}
