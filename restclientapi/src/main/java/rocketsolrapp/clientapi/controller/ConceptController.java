package rocketsolrapp.clientapi.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import rocketsolrapp.clientapi.datauploader.ConceptUploader;

@RestController
@RequestMapping("/concepts")
@ResponseStatus(value = HttpStatus.OK)
public class ConceptController {

    @Autowired
    ConceptUploader conceptUploader;

    @RequestMapping(value = "/reload", method = RequestMethod.GET)
    public void uploadConcepts() {
        conceptUploader.uploadConcepts();
    }
}
