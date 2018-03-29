package rocketsolrapp.clientapi.service;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rocketsolrapp.clientapi.model.Product;
import rocketsolrapp.clientapi.model.RequestWithParams;
import rocketsolrapp.clientapi.model.SKU;

import java.io.IOException;
import java.util.*;

@Service
public class ProductService {

    @Autowired
    SolrRequester solr;

    public List<Product> query(String coreName, RequestWithParams request) {

        final List<Product> result = new ArrayList<>();

        final SolrQuery query = new SolrQuery(request.getQuery());
        final QueryResponse response = solr.executeQuery(coreName, query);

        final Map<String, List<SolrDocument>> groupedDocuments = groupByRoot(response.getResults());

        for (String key: groupedDocuments.keySet()){
            final Product product = new Product();
            product.setId(key);
            for (SolrDocument document : groupedDocuments.get(key)){
                if (((String) document.getFieldValue("docType")).equals("product")){
                    product.setBrand( (String) document.getFieldValue("brand"));
                    product.setDepartment( (String) document.getFieldValue("department"));
                    product.setDescription( (String) document.getFieldValue("description"));
                    product.setPrice( (double) document.getFieldValue("price"));
                    product.setTitle( (String) document.getFieldValue("title"));
                } else {
                    final SKU sku = new SKU();
                    sku.setId( (String) document.getFieldValue("id"));
                    sku.setColor( (String) document.getFieldValue("color"));
                    sku.setSize( (String) document.getFieldValue("size"));
                    product.addSKU(sku);
                }
            }
            result.add(product);
        }
        return result;
    }

    public void add(String coreName, Product product) throws SolrServerException, IOException{
        final UpdateRequest request = new UpdateRequest();
        request.add(convertToSolrFormat(product), false);
        solr.sendSolrRequest(coreName, request);
    }

    public void clear(String coreName) throws SolrServerException, IOException {
        final UpdateRequest request = new UpdateRequest();
        request.deleteByQuery("*:*");
        solr.sendSolrRequest(coreName, request);
    }

    public Product update(String coreName, Product product) throws SolrServerException, IOException {
        final UpdateRequest request = new UpdateRequest();
        request.add(convertToSolrFormat(product), true);
        solr.sendSolrRequest(coreName, request);
        return product;
    }

    public void delete(String coreName, Product product) throws SolrServerException, IOException {
        final UpdateRequest request = new UpdateRequest();
        request.deleteByQuery("_root_:" + product.getId());
        solr.sendSolrRequest(coreName, request);
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

    private SolrInputDocument convertToSolrFormat(Product product){
        SolrInputDocument productDocument = new SolrInputDocument();
        productDocument.setField("id", product.getId());
        productDocument.setField("brand", product.getBrand());
        productDocument.setField("price", product.getPrice());
        productDocument.setField("docType", "product");
        productDocument.setField("title", product.getTitle());
        productDocument.setField("description", product.getDescription());
        productDocument.setField("department", product.getDepartment());
        for (SKU sku : product.getSkus()) {
            SolrInputDocument skuDocument = new SolrInputDocument();

            skuDocument.setField("docType", "SKU");
            skuDocument.setField("id", sku.getId());
            skuDocument.setField("color", sku.getColor());
            skuDocument.setField("size", sku.getSize());

            productDocument.addChildDocument(skuDocument);
        }
        return productDocument;
    }
}
