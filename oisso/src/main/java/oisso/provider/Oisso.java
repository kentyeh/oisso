package oisso.provider;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.Map;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.Message;
import org.openid4java.message.ParameterList;
import org.openid4java.server.ServerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class Oisso {

    private static final Logger logger = LogManager.getLogger(Oisso.class);
    public final static String ANONYMOUS_ACCOUNT = "anonymous";
    public final static String USER_INPUT_ACCOUNT = "account";
    public final static String USER_INFO = "userInfo";
    @Autowired
    @Qualifier("serverManager")
    private ServerManager serverManager;
    @Autowired
    @Qualifier("userAttributeFactory")
    private UserAttributeFactory userDataFactory;
    @Value("#{extAttrSchema}")
    private Map<String, String> extAttrSchema;
    @Autowired
    @Qualifier("messageAccessor")
    private MessageSourceAccessor messageAccessor;
    @Autowired
    private SimpleUrlLogoutSuccessHandler logoutSuccessHandler;
    @Value("#{appProperies['localeParam']}")
    private String localParamName;
    @Value("#{appProperies['logoutTargetUrlParameter']}")
    private String logoutTargetUrl;

    @ExceptionHandler(UnsupportedEncodingException.class)
    public String handleUnsupportedEncodingException(HttpServletRequest request, UnsupportedEncodingException ex) {
        request.setAttribute("exception", ex);
        return "error";
    }

    @RequestMapping("/")
    public String root(HttpServletRequest request, Principal principal) throws UnsupportedEncodingException {
        boolean chgLocal = localParamName != null && !localParamName.isEmpty() && request.getParameter(localParamName) != null
                && !request.getParameter(localParamName).isEmpty();
        if (principal == null) {
            logger.debug("***** principal is null");
            return request.getParameterMap().isEmpty() ? "login" : String.format("redirect:/%s%s", ANONYMOUS_ACCOUNT, CommonUtil.getParameters(request));
        } else if (request.getParameterMap().isEmpty() || (chgLocal && request.getParameterMap().size() == 1)) {
            logger.debug("***** principal is {}",principal.getName());
            String account = principal.getName();
            HttpSession session = request.getSession(true);
            if (session.getAttribute(USER_INFO) == null) {
                session.setAttribute(USER_INFO, userDataFactory.getUserAttribute(account));
            }
            return "userinfo";
        } else {
            logger.debug("***** redir to /{},{}",principal.getName(), CommonUtil.getParameters(request));
            return String.format("redirect:/%s%s", principal.getName(), CommonUtil.getParameters(request));
        }
    }

    @RequestMapping("/login")
    public String login(HttpServletRequest request, Principal principal) throws UnsupportedEncodingException {
        return principal == null || "anonymous".equals(principal.getName()) ? "login" : root(request, principal);
    }

    @RequestMapping("/{account}")
    public String accountEndPoint(@PathVariable("account") String account, HttpServletRequest request, HttpServletResponse response, Principal principal) throws IOException {
        logger.debug("accountEndPoint Account is {} ****\n{}-{}", account, request.getMethod(), CommonUtil.getParameters(request));
        if ("anonymous".equals(account)) {
            account = "";
        } else {
            if (principal != null && !principal.getName().equals(account)) {
                logger.debug("Another user({}) login from same machine, invalidate previous user({}).", account, principal.getName());
                HttpSession session = request.getSession();
                if (session != null) {
                    session.invalidate();
                }
                return String.format("redirect:/%s%s", account, CommonUtil.getParameters(request));
            }
            request.setAttribute(USER_INPUT_ACCOUNT, account);
        }
        if (request.getParameterMap().isEmpty()) {
            logger.debug("RequestURI is {},return a xrds definition.", request.getRequestURI());
            return "xrds";
        } else {
            logger.debug("RequestURI is {}{}", request.getRequestURI(), CommonUtil.getParameters(request));
            String mode = request.getParameter("openid.mode");
            if ("associate".equals(mode) || "check_authentication".equals(mode)) {
                Message message = "associate".equals(mode)
                        ? serverManager.associationResponse(new ParameterList(request.getParameterMap()))
                        : serverManager.verify(new ParameterList(request.getParameterMap()));
                response.setContentType("text/plain");
                OutputStream out = response.getOutputStream();
                out.write(message.keyValueFormEncoding().getBytes("UTF-8"));
                out.close();
                return null;
            } else if ("checkid_immediate".equals(mode) || "checkid_setup".equals(mode)) {
                HttpSession session = request.getSession(true);
                session.setAttribute("loginId", account);
                session.setAttribute("parameterList", new ParameterList(request.getParameterMap()));
                return "redirect:/loginBeforeReturn?account=" + URLEncoder.encode(account, "UTF-8");
            } else {
                if (mode != null && !mode.isEmpty()) {
                    request.setAttribute("errorMessage", String.format("%s:%s", messageAccessor.getMessage("oisso.unknown.open.mode"), mode));
                }
                return "login";
            }
        }
    }

    @RequestMapping("/loginBeforeReturn")
    @PreAuthorize("isAuthenticated()")
    public String returnAfterLogin(HttpServletRequest request, HttpServletResponse response, Principal principal) throws IOException {
        HttpSession session = request.getSession();
        session.removeAttribute("loginId");
        ParameterList parameterList = (ParameterList) session.getAttribute("parameterList");
        String userId = principal.getName();
        String identifier = request.getParameter("account");
        identifier = identifier == null || identifier.isEmpty() ? "anonymous" : identifier;

        logger.debug("returnAfterLogin:user is {} & account is \"{}\"", userId, identifier);
        String oPEndpointUrl = CommonUtil.getContextPath(request) + "/" + identifier;
        serverManager.setOPEndpointUrl(oPEndpointUrl);
        Message authResponse = CommonUtil.buildAuthResponse(serverManager, parameterList,
                oPEndpointUrl, oPEndpointUrl, userDataFactory.getUserAttribute(userId), extAttrSchema);
        session.removeAttribute("parameterList");
        if (authResponse instanceof AuthSuccess) {
            String redir = authResponse.getDestinationUrl(true);
            logger.debug("redirect url to {}", redir);
            return String.format("redirect:%s", redir);
        } else {
            ServletOutputStream os = response.getOutputStream();
            os.write(authResponse.keyValueFormEncoding().getBytes());
            os.close();
            return null;
        }
    }
}
