package rocketsolrapp.clientapi.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import rocketsolrapp.clientapi.datauploader.ConceptUploader;
import rocketsolrapp.clientapi.service.ProductService;

@RestController
@RequestMapping("/dataload")
@ResponseStatus(value = HttpStatus.OK)
public class DataloadController {

    @Autowired
    ConceptUploader conceptUploader;

    @Autowired
    ProductService productService;

    @RequestMapping(value = "/concepts", method = RequestMethod.GET)
    public void reloadConcepts() {
        conceptUploader.uploadConcepts();
    }

    @RequestMapping(value = "/products", method = RequestMethod.GET)
    public void reloadProducts() throws Exception{
        productService.reloadData();
    }

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public void reloadAll() throws Exception{
        reloadProducts();
        reloadConcepts();
    }
}
