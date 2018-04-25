package rocketsolrapp.clientapi.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import rocketsolrapp.clientapi.model.product.Product;
import rocketsolrapp.clientapi.model.RequestWithParams;
import rocketsolrapp.clientapi.model.SearchResponse;
import rocketsolrapp.clientapi.service.ProductService;

@RestController
@ResponseStatus(value = HttpStatus.OK)
public class ProductController {

    @Autowired
    ProductService productService;

    @RequestMapping(value = "/query", method = RequestMethod.GET)
    public SearchResponse query(@ModelAttribute RequestWithParams requestWithParams)
            throws Exception {
        return productService.query(requestWithParams);
    }

    @RequestMapping(value = "/query", method = RequestMethod.DELETE)
    public void delete(@ModelAttribute Product product) throws Exception {
        productService.delete(product);
    }

    @RequestMapping(value = "/query", method = RequestMethod.PUT)
    public void add(@ModelAttribute Product product) throws Exception {
        productService.add(product);
    }

    @RequestMapping(value = "/query", method = RequestMethod.POST)
    public void update(@ModelAttribute Product product) throws Exception {
        productService.update(product);
    }

    @RequestMapping(value = "/clear", method = RequestMethod.GET)
    public void clear() throws Exception {
        productService.clear();
    }

    @RequestMapping(value = "setinv/{skuId}", method = RequestMethod.POST)
    public void setInv(@PathVariable String skuId, @RequestParam String inv) throws Exception{
        productService.updateInventory(skuId, inv);
    }

}
