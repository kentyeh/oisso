package oisso.provider;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.minidev.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.util.StringUtils;

/**
 *
 * @author Kent Yeh
 */
public class OissoLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

    private static final Logger logger = LogManager.getLogger(OissoLogoutSuccessHandler.class);

    private static final String csrfHeader;
    private static final String csrfParameter;

    @Autowired
    @Qualifier("messageAccessor")
    MessageSourceAccessor messageAccessor;
    @Value("#{appProperies['httpPort']}")
    private String httpPort;

    static {
        CsrfToken token = new HttpSessionCsrfTokenRepository().generateToken(null);
        csrfHeader = token.getHeaderName();
        csrfParameter = token.getParameterName();
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            response.setCharacterEncoding("UTF-8");
            if (StringUtils.hasText(request.getParameter("callback"))) {
                response.setContentType("application/x-javascript");
                response.getWriter().write(request.getParameter("callback") + "('" + determineTargetUrl(request, response) + "')");
            } else {
                response.setContentType("application/json");
                JSONObject json = new JSONObject();
                json.put(getTargetUrlParameter(), determineTargetUrl(request, response));
                response.getWriter().write(json.toString());
            }
        } else {
            String csrf = request.getParameter(csrfHeader);
            csrf = StringUtils.hasText(csrf) ? csrf : request.getParameter(csrfParameter);
            if (StringUtils.hasText(csrf)) {
                response.setCharacterEncoding("UTF-8");
                response.setContentType("text/html");
                StringBuilder sb = new StringBuilder("<!DOCTYPE html>")
                        .append("<!DOCTYPE html>").append("<html><head><title>")
                        .append(messageAccessor.getMessage("userinfo.logout"))
                        .append("</title></head>")
                        .append("<body onload=\"document.getElementById('_csrf').submit()\">")
                        .append("<form action=\"").append(determineTargetUrl(request, response))
                        .append("\" method=\"post\" style=\"display: inline\" id=\"_csrf\">")
                        .append("<div align=\"center\"><img src=\"").append(request.getContextPath())
                        .append("/images/oisso.png\"/><br/>")
                        .append("<input type=\"hidden\" name=\"").append(csrfParameter)
                        .append("\" value=\"").append(csrf).append("\"/>")
                        .append("<input type=\"submit\" value=\"")
                        .append(messageAccessor.getMessage("logout.continue")).append("\"/>")
                        .append("</div></form></body></html>");
                response.getWriter().write(sb.toString());
            } else {
                super.onLogoutSuccess(request, response, authentication); //To change body of generated methods, choose Tools | Templates.
            }
        }
    }

}
