package rocketsolrapp.clientapi.model;

import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.common.util.ContentStreamBase;

import java.util.Collection;
import java.util.Collections;

@SuppressWarnings("serial")
public class SolrTaggerRequest extends QueryRequest {
    private String input;

    public SolrTaggerRequest(SolrParams p, SolrRequest.METHOD m) {
        super(p, m);
    }

    /**
     * @return the input
     */
    public String getInput() {
        return input;
    }

    /**
     * @param input
     *            the input to set
     */
    public void setInput(String input) {
        this.input = input;
    }

    @Override
    public Collection<ContentStream> getContentStreams() {
        ContentStreamBase.StringStream stream = new ContentStreamBase.StringStream(input);
        stream.setContentType("application/octet-stream");
        return Collections.singleton((ContentStream) stream);
    }
}