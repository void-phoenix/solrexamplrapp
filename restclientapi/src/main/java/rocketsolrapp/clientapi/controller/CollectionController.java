package rocketsolrapp.clientapi.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import rocketsolrapp.clientapi.model.Product;
import rocketsolrapp.clientapi.model.RequestWithParams;
import rocketsolrapp.clientapi.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/query")
@ResponseStatus(value = HttpStatus.OK)
public class CollectionController {

    @Autowired
    ProductService productService;

    @RequestMapping(method = RequestMethod.GET)
    public List<Product> query(@ModelAttribute RequestWithParams requestWithParams)
            throws Exception {
        return productService.query(requestWithParams);
    }

    @RequestMapping(method = RequestMethod.DELETE)
    public void delete(@ModelAttribute Product product) throws Exception {
        productService.delete(product);
    }

    @RequestMapping(method = RequestMethod.PUT)
    public void add(@ModelAttribute Product product) throws Exception {
        productService.add(product);
    }

    @RequestMapping(method = RequestMethod.POST)
    public void update(@ModelAttribute Product product) throws Exception {
        productService.update(product);
    }

    @RequestMapping(value = "/clear", method = RequestMethod.GET)
    public void clear() throws Exception {
        productService.clear();
    }
}
