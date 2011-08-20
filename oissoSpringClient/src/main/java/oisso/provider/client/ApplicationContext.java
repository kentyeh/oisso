package oisso.provider.client;

import javax.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.context.ServletContextAware;

@Configuration
@ImportResource("classpath:applicationContext.xml")
public class ApplicationContext implements ServletContextAware {

    ServletContext servletContext;
    @Value("${openid.provider.directLoginUrl}")
    private String directLogin;
    @Value("${openid.provider.logoutUrl}")
    private String logoutUrl;

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
        servletContext.setAttribute("directLoginUrl", directLogin);
        servletContext.setAttribute("providerLogoutUrl", logoutUrl);
    }
}
