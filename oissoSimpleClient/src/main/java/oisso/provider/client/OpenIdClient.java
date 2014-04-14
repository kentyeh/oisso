package oisso.provider.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openid4java.association.AssociationException;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.MessageException;
import org.openid4java.message.MessageExtension;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;
import org.openid4java.message.sreg.SRegMessage;
import org.openid4java.message.sreg.SRegRequest;
import org.openid4java.message.sreg.SRegResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class OpenIdClient {

    private static final Logger logger = LogManager.getLogger(OpenIdClient.class);
    @Autowired
    @Qualifier("consumerManager")
    private ConsumerManager consumerManager;
    private String oissoContextPath;
    @Value("${openid.provider.logoutUrl}")
    private String logoutUrl;
    @Value("#{extAttrSchema}")
    private Map<String, String> extAttrSchema;
    @Autowired
    @Qualifier("messageAccessor")
    MessageSourceAccessor messageAccessor;

    @Value("${oisso.contexPath}")
    public void setOissoContextPath(String oissoContextPath) {
        this.oissoContextPath = oissoContextPath.endsWith("/") ? oissoContextPath : oissoContextPath + "/";
    }

    private String getOpEndPoint(HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (session != null) {
            String identifier = (String) session.getAttribute("identifier");
            return identifier == null || identifier.isEmpty() ? this.oissoContextPath + "anonymous" : identifier;
        }
        return this.oissoContextPath + "anonymous";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(@RequestParam("identifier") String identifier, HttpServletRequest request) {
        if (identifier.indexOf('/') == -1) {
            identifier = oissoContextPath + identifier;
        }
        request.getSession().setAttribute("identifier", identifier);
        return "redirect:/userinfo";
    }

    @RequestMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (session != null) {
            session.invalidate();
        }
        /*Client logout is not working if you already have log into provider server,
         * and clearily, logout from provider server don't mean client logout,
         * If you had many clients, log out this client and server, don't mean other client logout.
         */
        //logger.debug("Redirect to Open Id Provider to processing logout.");
        //You need thinking whether it is nessary to logout  openid provider, or just logout locally.
        //return String.format("redirect:%s", logoutUrl);
        return "redirect:/";
    }

    @RequestMapping("/userinfo")
    public String userinfo(HttpServletRequest request) throws DiscoveryException, MessageException, ConsumerException {
        HttpSession session = request.getSession();
        if (session != null && session.getAttribute("userInfo") != null) {
            logger.debug("User already loggin.");
            return "userinfo";
        } else {
            DiscoveryInformation di = null;
            logger.debug("Execute discover with {}.", getOpEndPoint(request));
            List<DiscoveryInformation> discoveries = consumerManager.discover(getOpEndPoint(request));
            di = consumerManager.associate(discoveries);
            logger.debug("Save discover infomation.");
            if (session == null) {
                session = request.getSession(true);
            }
            session.setAttribute("discoveryInformation", di);
            logger.debug("Build Auth Request");
            AuthRequest authRequest = consumerManager.authenticate(di, getContextPath(request) + "/oissoReturnPath");
            logger.debug("Add simple Registration attributes.");
            SRegRequest sRegRequest = SRegRequest.createFetchRequest();
            //I use nickname to store user identifier, so it's requied.
            sRegRequest.addAttribute("nickname", true);
            sRegRequest.addAttribute("fullname", false);
            authRequest.addExtension(sRegRequest);
            logger.debug("Add extension exchange attributes");
            FetchRequest fetchRequest = FetchRequest.createFetchRequest();
            for (String attr : extAttrSchema.keySet()) {
                fetchRequest.addAttribute(attr, extAttrSchema.get(attr), false);
            }
            authRequest.addExtension(fetchRequest);
            String redir = authRequest.getDestinationUrl(true);
            logger.debug("Redirect to provider:{}", redir);
            return String.format("redirect:%s", redir);
        }
    }

    @RequestMapping("/oissoReturnPath")
    public String userinfoReturn(HttpServletRequest request) throws IllegalArgumentException, MessageException, DiscoveryException, AssociationException {
        HttpSession session = request.getSession();
        if (session == null) {
            throw new IllegalArgumentException(messageAccessor.getMessage("client.userinfoReturn.exception1"));
        } else {
            logger.debug("Start to processing open-id return. \bFirst thing is to get discovry information previous reservation,");
            DiscoveryInformation di = (DiscoveryInformation) session.getAttribute("discoveryInformation");
            if (di == null) {
                throw new IllegalArgumentException(messageAccessor.getMessage("client.userinfoReturn.exception2"));
            } else {
                logger.debug("Di \nversion={},\nendpoint={},\nclaimedIdentifier={},\ndelegateIdentifier={}", new Object[]{di.getVersion(), di.getOPEndpoint(), di.getClaimedIdentifier(), di.getDelegateIdentifier()});
                ParameterList response = new ParameterList(request.getParameterMap());
                VerificationResult verificationResult = consumerManager.verify(getContextPath(request) + "/oissoReturnPath", response, di);
                Identifier verifiedIdentifier = verificationResult.getVerifiedId();
                if (verifiedIdentifier != null) {
                    AuthSuccess authSuccess = (AuthSuccess) verificationResult.getAuthResponse();
                    Map<String, String> userInfo = new HashMap<String, String>();
                    if (authSuccess.hasExtension(SRegMessage.OPENID_NS_SREG)) {
                        logger.debug("Retrive simple registration attributes.");
                        MessageExtension extension = authSuccess.getExtension(SRegMessage.OPENID_NS_SREG);
                        if (extension instanceof SRegResponse) {
                            SRegResponse sRegResponse = (SRegResponse) extension;
                            userInfo.put("idenifier", verifiedIdentifier.getIdentifier());
                            userInfo.put("nickname", sRegResponse.getAttributeValue("nickname"));
                            userInfo.put("fullname", sRegResponse.getAttributeValue("fullname"));
                        }
                    }
                    if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
                        logger.debug("Retrive extension exchange attribute");
                        MessageExtension extension = authSuccess.getExtension(AxMessage.OPENID_NS_AX);
                        if (extension instanceof FetchResponse) {
                            FetchResponse fetchResponse = (FetchResponse) extension;
                            for (String attr : extAttrSchema.keySet()) {
                                userInfo.put(attr, fetchResponse.getAttributeValue(attr));
                            }
                        }
                    }
                    session.setAttribute("userInfo", userInfo);
                    return "redirect:/userinfo";
                } else {
                    throw new IllegalArgumentException(messageAccessor.getMessage("client.userinfoReturn.exception3"));
                }
            }
        }
    }

    @ExceptionHandler({DiscoveryException.class, MessageException.class, ConsumerException.class, AssociationException.class, IllegalArgumentException.class})
    public String error(Exception exception, HttpServletRequest request) {
        logger.error(exception.getMessage(), exception);
        request.setAttribute("exception", exception);
        return "error";
    }

    public static String getContextPath(HttpServletRequest request) {
        StringBuffer pb = new StringBuffer();
        pb.append(request.getScheme()).append("://").append(request.getServerName());
        if (request.getLocalPort() != 80) {
            pb.append(":").append(request.getLocalPort());
        }
        pb.append(request.getContextPath());
        return pb.toString();
    }
}
