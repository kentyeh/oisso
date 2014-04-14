package oisso.provider;

import java.io.IOException;
import java.lang.reflect.Method;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

/**
 *
 * <div>Indicate login need for ajax like request.<br>
 * Notice:No assurance that request headers will be reserved after multiple
 * redirection.</div>
 * <br>
 * <div>若是請求來自Ajax，則不須導向Login，直接通知前端未授權.<br>
 * 注意：多次轉向後，不保證瀏覽器還會保留原來的請求表頭。</div>
 *
 * @author Kent Yeh
 */
public class AjaxAwareLoginUrlAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {

    private static final Logger logger = LogManager.getLogger(AjaxAwareLoginUrlAuthenticationEntryPoint.class);

    @Autowired
    @Qualifier("messageAccessor")
    private MessageSourceAccessor messageAccessor;

    public AjaxAwareLoginUrlAuthenticationEntryPoint(String loginFormUrl) {
        super(loginFormUrl);
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        boolean isGwt = (request.getHeader("x-gwtsecurity-rf-request") != null && "true".equalsIgnoreCase(request.getHeader("x-gwtsecurity-rf-request")))
                || (request.getContentType() != null && request.getContentType().startsWith("text/x-gwt-rpc"));
        Class<?> GwtResponseUtil = null;
        if (isGwt) {
            try {
                GwtResponseUtil = Class.forName("com.gwt.ss.GwtResponseUtil");
            } catch (ClassNotFoundException ex) {
                isGwt = false;
            }
        }
        if (isGwt) {
            if (response.isCommitted()) {
                logger.error("Gwt response had already sent.");
            }else{
                try {
                    Method method = GwtResponseUtil.getMethod("processGwtExceptio",
                            ServletContext.class, HttpServletRequest.class,
                            HttpServletResponse.class, Exception.class);
                    if (method == null) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        method.invoke(null, request.getSession().getServletContext(), request, response,
                                new InsufficientAuthenticationException(messageAccessor.getMessage("login.account.placeholder")));
                    }
                } catch (Exception ex) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                }
            }
        } else if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            super.commence(request, response, authException);
        }
    }
}
