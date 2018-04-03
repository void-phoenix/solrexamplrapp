package rocketsolrapp.datauploader;

import org.springframework.beans.factory.annotation.Autowired;
import rocketsolrapp.solr.factory.SolrClientFactory;

public class ConceptUploader {

    @Autowired
    SolrClientFactory clientFactory;

    public void uploadConcepts(){

    }
}
