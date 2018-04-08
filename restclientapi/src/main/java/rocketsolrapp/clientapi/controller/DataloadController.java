package rocketsolrapp.clientapi.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import rocketsolrapp.clientapi.datauploader.DataUploader;
import rocketsolrapp.clientapi.service.ProductService;

@RestController
@RequestMapping("/dataload")
@ResponseStatus(value = HttpStatus.OK)
public class DataloadController {

    @Autowired
    DataUploader dataUploader;

    @RequestMapping(value = "/concepts", method = RequestMethod.GET)
    public void reloadConcepts() throws Exception {
        dataUploader.reloadConcepts();
    }

    @RequestMapping(value = "/products", method = RequestMethod.GET)
    public void reloadProducts() throws Exception{
        dataUploader.reloadProducts();
    }

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public void reloadAll() throws Exception{
        reloadProducts();
        reloadConcepts();
    }
}

