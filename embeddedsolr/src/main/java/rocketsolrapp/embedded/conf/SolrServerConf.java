package rocketsolrapp.embedded.conf;

import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.NodeConfig;
import org.apache.solr.servlet.LoadAdminUiServlet;
import org.apache.solr.servlet.SolrDispatchFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Properties;

@Configuration
public class SolrServerConf {

    public static final int ORDER = -11000;
    @Autowired
    ServletContext servletContext;
    //@Value("${solr-config.home}")
    private String solrHome;

    @Bean(name = "SolrDispatchFilter")
    public SolrDispatchFilter solrDispatchFilter() {
        return new SolrDispatchFilter() {
            @Override
            protected CoreContainer createCoreContainer(Path solrHome, Properties extraProperties) {
                final NodeConfig nodeConfig = loadNodeConfig(solrHome, extraProperties);
                cores = new CoreContainer(nodeConfig, extraProperties);
                cores.load();
                return cores;
            }

            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
                if (response instanceof HttpServletResponse) {
                    ((HttpServletResponse) response).setHeader("Access-Control-Allow-Origin", "*");
                }
                super.doFilter(request, response, chain);
            }
        };
    }

    @Bean(name = "solrDispatchFilterRegistration")
    public FilterRegistrationBean solrRequestFilterRegistration() throws Exception {
        System.setProperty("solr.solr.home", "/Users/dpanchenko/Downloads/downloads/gdsolr");
        System.setProperty("solr.data.home", "/Users/dpanchenko/Downloads/downloads/gdsolr/indexes");

        final FilterRegistrationBean<SolrDispatchFilter> filterFilterRegistrationBean = new FilterRegistrationBean<>();
        filterFilterRegistrationBean.setFilter(solrDispatchFilter());
        filterFilterRegistrationBean.addUrlPatterns("/*");
        filterFilterRegistrationBean.addServletRegistrationBeans(servletRegistrationBean());
        filterFilterRegistrationBean.addInitParameter("excludePatterns", "/css/.+,/js/.+,/img/.+,/tpl/.+");
        filterFilterRegistrationBean.addInitParameter("path-prefix", "/solr");
        filterFilterRegistrationBean.setName("SolrRequestFilter");
        filterFilterRegistrationBean.setOrder(ORDER);
        return filterFilterRegistrationBean;
    }


    @Bean
    public ServletRegistrationBean servletRegistrationBean() throws Exception {
        return new ServletRegistrationBean(new LoadAdminUiServlet(), "/admin.html");
    }


}
