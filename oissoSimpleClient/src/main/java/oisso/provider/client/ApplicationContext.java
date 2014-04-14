package oisso.provider.client;

import javax.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.context.ServletContextAware;

@Configuration
@ImportResource("classpath:applicationContext.xml")
public class ApplicationContext implements ServletContextAware {

    @Value("${openid.provider.logoutUrl}")
    private String logoutUrl;

    @Override
    public void setServletContext(ServletContext servletContext) {
        servletContext.setAttribute("providerLogoutUrl", logoutUrl);
    }
}
