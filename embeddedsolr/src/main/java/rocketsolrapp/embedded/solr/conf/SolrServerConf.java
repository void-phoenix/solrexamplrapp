package rocketsolrapp.embedded.solr.conf;

import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.NodeConfig;
import org.apache.solr.servlet.LoadAdminUiServlet;
import org.apache.solr.servlet.SolrDispatchFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

@Configuration
public class SolrServerConf {

    public static final int ORDER = -11000;
    @Value("${solr-config.home}")
    private String solrHome;

    @Bean(name = "solrDispatchFilterRegistration")
    public FilterRegistrationBean solrRequestFilterRegistration() {
        System.setProperty("solr.solr.home", solrHome);
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(solrDispatchFilter());
        registration.addUrlPatterns("/*");
        registration.addServletRegistrationBeans(servletRegistrationBean());
        registration.addInitParameter("excludePatterns", "/css/.+,/js/.+,/img/.+,/tpl/.+");
        registration.addInitParameter("path-prefix", "/solr");
        registration.setName("SolrRequestFilter");
        // Before all Spring filters except for
        // springSecurityFilterChain registration described in WebSecurityConfig
        registration.setOrder(ORDER);
        return registration;
    }

    @Bean(name = "SolrDispatchFilter")
    public SolrDispatchFilter solrDispatchFilter() {
        return new SolrDispatchFilter() {
            protected CoreContainer createCoreContainer(Path solrHome, Properties extraProperties) {
                NodeConfig nodeConfig = loadNodeConfig(solrHome, extraProperties);
                cores = new CoreContainer(nodeConfig, extraProperties, false);
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

    @Bean
    public ServletRegistrationBean servletRegistrationBean() {
        return new ServletRegistrationBean(new LoadAdminUiServlet(), "/admin.html");
    }

}
