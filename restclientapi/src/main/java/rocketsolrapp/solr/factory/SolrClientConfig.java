package rocketsolrapp.solr.factory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties
public class SolrClientConfig {

    public enum SolrJtype {
        HTTP("http"), CLOUD("cloud"), EMBEDDED("embedded");

        private static Map<String, SolrJtype> map = new HashMap<>();

        static {
            for (SolrJtype type : SolrJtype.values()) {
                map.put(type.typeStr, type);
            }
        }

        private final String typeStr;

        SolrJtype(String typeStr) {
            this.typeStr = typeStr;
        }

        public String getTypeStr() {
            return typeStr;
        }

        public static SolrJtype getByTypeStr(String typeStr) {
            return map.get(typeStr);
        }
    }

    @Value("${solr.url}")
    private String urlString;

    @Value("${sorl.zkHostString}")
    private String zkHostString;

    @Value("${solr.client.type}")
    private String solrJtype;

    public SolrJtype getSolrJtype() {
        return SolrJtype.getByTypeStr(solrJtype);
    }

    public String getZkHostString() {
        return zkHostString;
    }

    public String getUrlString() {
        return urlString;
    }
}
