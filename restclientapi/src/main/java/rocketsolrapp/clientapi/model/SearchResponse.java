package rocketsolrapp.clientapi.model;

import java.util.List;

public class SearchResponse {

    private List<Product> products;
    private List<Facet> facets;

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public List<Facet> getFacets() {
        return facets;
    }

    public void setFacets(List<Facet> facets) {
        this.facets = facets;
    }
}
