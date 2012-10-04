package oisso.provider;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.openid4java.message.Message;
import org.openid4java.message.ParameterList;
import org.openid4java.server.ServerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class Oisso {

    private static Logger logger = LoggerFactory.getLogger(Oisso.class);
    public final static String ANONYMOUS_ACCOUNT = "anonymous";
    public final static String USER_INPUT_ACCOUNT = "account";
    public final static String USER_INFO = "userInfo";
    @Autowired
    @Qualifier("serverManager")
    private ServerManager serverManager;
    @Autowired
    @Qualifier("userAttributeFactory")
    UserAttributeFactory userDataFactory;
    @Value("#{extAttrSchema}")
    Map<String, String> extAttrSchema;
    @Autowired
    @Qualifier("messageAccessor")
    MessageSourceAccessor messageAccessor;
    @Value("#{appProperies['localeParam']}")
    String localParamName;

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
            return request.getParameterMap().isEmpty() ? "login" : String.format("redirect:/%s%s", ANONYMOUS_ACCOUNT, CommonUtil.getParameters(request));
        } else if (request.getParameterMap().isEmpty() || (chgLocal && request.getParameterMap().size() == 1)) {
            String account = principal.getName();
            HttpSession session = request.getSession(true);
            if (session.getAttribute(USER_INFO) == null) {
                session.setAttribute(USER_INFO, userDataFactory.getUserAttribute(account));
            }
            return "userinfo";
        } else {
            return String.format("redirect:/%s%s", principal.getName(), CommonUtil.getParameters(request));
        }
    }

    @RequestMapping("/login")
    public String login(HttpServletRequest request, Principal principal) throws UnsupportedEncodingException {
        return principal==null||"anonymous".equals(principal.getName())?"login":root(request, principal);
    }

    @RequestMapping("/{account}")
    public String accountEndPoint(@PathVariable("account") String account, HttpServletRequest request, HttpServletResponse response, Principal principal) throws IOException {
        logger.debug("accountEndPoint Account is {}", account);
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
            if ("associate".equals(mode)) {
                Message message = serverManager.associationResponse(new ParameterList(request.getParameterMap()));
                response.setContentType("text/plain");
                OutputStream out = response.getOutputStream();
                out.write(message.keyValueFormEncoding().getBytes("UTF-8"));
                out.close();
                return null;
            } else if ("checkid_immediate".equals(mode) || "checkid_setup".equals(mode) || "check_authentication".equals(mode)) {
                HttpSession session = request.getSession(true);
                session.setAttribute("loginId", account);
                return "redirect:/loginBeforeReturn" + CommonUtil.getParameters(request, "account=" + account);
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
    public String returnAfterLogin(HttpServletRequest request, Principal principal) {
        HttpSession session = request.getSession();
        if (session != null) {
            session.removeAttribute("loginId");
        }
        String userId = principal.getName();
        String identifier = request.getParameter("account");
        identifier = identifier == null || identifier.isEmpty() ? "anonymous" : identifier;

        logger.debug("returnAfterLogin:user is {} & account is \"{}\"", userId, identifier);
        String oPEndpointUrl = CommonUtil.getContextPath(request) + "/" + identifier;
        serverManager.setOPEndpointUrl(oPEndpointUrl);
        Message authResponse = CommonUtil.buildAuthResponse(serverManager, new ParameterList(request.getParameterMap()),
                oPEndpointUrl, oPEndpointUrl, userDataFactory.getUserAttribute(userId), extAttrSchema);
        String redir = authResponse.getDestinationUrl(true);
        logger.debug("redirect url to {}", redir);
        return String.format("redirect:%s", redir);
    }
}
