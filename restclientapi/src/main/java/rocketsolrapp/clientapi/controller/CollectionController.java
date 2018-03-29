package rocketsolrapp.clientapi.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import rocketsolrapp.clientapi.model.Product;
import rocketsolrapp.clientapi.model.RequestWithParams;
import rocketsolrapp.clientapi.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/{coreName}")
@ResponseStatus(value = HttpStatus.OK)
public class CollectionController {

    @Autowired
    ProductService productService;

    @RequestMapping(method = RequestMethod.GET)
    public List<Product> query(@PathVariable String coreName, @ModelAttribute RequestWithParams requestWithParams)
            throws Exception {
        return productService.query(coreName, requestWithParams);
    }

    @RequestMapping(method = RequestMethod.DELETE)
    public void delete(@PathVariable String coreName, @ModelAttribute Product product) throws Exception {
        productService.delete(coreName, product);
    }

    @RequestMapping(method = RequestMethod.PUT)
    public void add(@PathVariable String coreName, @ModelAttribute Product product) throws Exception {
        productService.add(coreName, product);
    }

    @RequestMapping(method = RequestMethod.POST)
    public void update(@PathVariable String coreName, @ModelAttribute Product product) throws Exception {
        productService.update(coreName, product);
    }

    @RequestMapping(value = "/clear", method = RequestMethod.GET)
    public void clear(@PathVariable String coreName) throws Exception {
        productService.clear(coreName);
    }
}
