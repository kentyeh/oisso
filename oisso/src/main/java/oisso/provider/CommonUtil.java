package oisso.provider;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import javax.servlet.http.HttpServletRequest;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.Message;
import org.openid4java.message.MessageExtension;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;
import org.openid4java.message.sreg.SRegMessage;
import org.openid4java.message.sreg.SRegRequest;
import org.openid4java.message.sreg.SRegResponse;
import org.openid4java.server.ServerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonUtil {

    private static Logger logger = LoggerFactory.getLogger(CommonUtil.class);
//    private final static String[] OPENID_NS_SREG_FIELDS = new String[]{"nickname", "email", "fullname", "dob", "gender", "postcode", "country", "language", "timezone"};
    private static Map<String, String> sregSchema = new HashMap<String, String>();

    public static void setSregSchema(Map<String, String> sregSchema) {
        CommonUtil.sregSchema = sregSchema;
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

    public static String getParameters(HttpServletRequest request) throws UnsupportedEncodingException {
        return getParameters(request, new HashMap<String, String>());
    }

    public static String getParameters(HttpServletRequest request, String... paramPairs) throws UnsupportedEncodingException {
        HashMap<String, String> map = new HashMap<String, String>();
        for (String paramPair : paramPairs) {
            StringTokenizer st = new StringTokenizer(paramPair, "=");
            String key = st.hasMoreTokens() ? st.nextToken() : "";
            String value = st.hasMoreTokens() ? st.nextToken() : "";
            if (!key.isEmpty()) {
                map.put(key, value);
            }
        }
        return getParameters(request, map);
    }

    public static String getParameters(HttpServletRequest request, Map<String, String> additionalParams) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder("");
        Map<String, Object> paramap = request.getParameterMap();
        for (String para : paramap.keySet()) {
            sb.append(sb.length() == 0 ? "?" : "&").append(para).append("=");
            String[] values = request.getParameterValues(para);
            String value = "";
            for (int i = 0; i < values.length; i++) {
                if (i == 0) {
                    value = values[i];
                } else {
                    value = value + "," + values[i];
                }
            }
            if (!value.isEmpty()) {
                sb.append(URLEncoder.encode(values[0], "UTF-8"));
            }
        }
        if (additionalParams != null && !additionalParams.isEmpty()) {
            for (String para : additionalParams.keySet()) {
                sb.append(sb.length() == 0 ? "?" : "&").append(para).append("=");
                String value = additionalParams.get(para);
                if (value != null && !value.isEmpty()) {
                    sb.append(URLEncoder.encode(value, "UTF-8"));
                }
            }
        }
        return sb.toString();
    }

    public static Message buildAuthResponse(ServerManager serverManager, ParameterList requestParameters,
            String userSelectedId, String userSelectedClaimedId, Map<String, ?> userAttribute, Map<String, String> extensionAttrSchema) {
        Message authResponse = serverManager.authResponse(requestParameters, userSelectedId, userSelectedClaimedId, true);
        AuthRequest authRequest = null;
        try {
            boolean hasSreg = false;
            authRequest = AuthRequest.createAuthRequest(requestParameters, serverManager.getRealmVerifier());
            logger.debug("Processing Simple Registration attributes…");
            if (authRequest.hasExtension(SRegMessage.OPENID_NS_SREG)) {
                MessageExtension extensionRequestObject = authRequest.getExtension(SRegMessage.OPENID_NS_SREG);
                if (extensionRequestObject instanceof SRegRequest) {
                    hasSreg = true;
                    SRegRequest sRegRequest = (SRegRequest) extensionRequestObject;
                    Map<String, String> simpleAttribute = new HashMap<String, String>();
                    for (String regext : sregSchema.keySet()) {
                        if (userAttribute.containsKey(regext)) {
                            logger.debug("add Attribute {}:{}", regext, userAttribute.get(regext).toString());
                            simpleAttribute.put(regext, userAttribute.get(regext).toString());
                        }
                    }
                    SRegResponse sRegResponse = SRegResponse.createSRegResponse(sRegRequest, simpleAttribute);
                    authResponse.addExtension(sRegResponse);
                } else {
                    logger.error("Cannot continue processing Simple Registration Extension. The object returned from the AuthRequest (of type {}) claims to be correct, but is not of type {} as expected.",
                            extensionRequestObject.getClass().getName());
                }
            }
            if (authRequest.hasExtension(AxMessage.OPENID_NS_AX)) {
                logger.debug("Processing Exchange extension attributes …");
                MessageExtension extensionRequestObject = authRequest.getExtension(AxMessage.OPENID_NS_AX);
                FetchResponse fetchResponse = null;
                if (extensionRequestObject instanceof FetchRequest) {
                    FetchRequest axRequest = (FetchRequest) extensionRequestObject;
                    ParameterList parameters = axRequest.getParameters();
                    Map<String, String> axData = new HashMap<String, String>();
                    fetchResponse = FetchResponse.createFetchResponse(axRequest, axData);
                    //Because Spring Security default not recorgnizing simple registration, so we need to translate it to Ax Message;
                    if (!hasSreg) {
                        for (String key : sregSchema.keySet()) {
                            Object value = userAttribute.get(key);
                            if (value != null) {
                                logger.debug("add simple registration attribute {}:{}", key, userAttribute.get(key).toString());
                                axData.put(key, sregSchema.get(key));
                                fetchResponse.addAttribute(key, sregSchema.get(key), value.toString());
                            }
                        }
                    }
                    for (String key : extensionAttrSchema.keySet()) {
                        if (parameters.hasParameter("type." + key)) {
                            logger.debug("add extended attribute {}:{}", key, userAttribute.get(key).toString());
                            axData.put(key, userAttribute.get(key).toString());
                            fetchResponse.addAttribute(key, extensionAttrSchema.get(key), userAttribute.get(key).toString());
                        }
                    }
                    authResponse.addExtension(fetchResponse);
                } else {
                    logger.error("Cannot continue processing Attribute Exchange (AX) request. The object returned from the AuthRequest (of type {}) claims to be correct, but is not of type {} as expected.",
                            extensionRequestObject.getClass().getName(), AxMessage.class.getName());
                }
            }
            serverManager.sign((AuthSuccess) authResponse);
        } catch (Exception e) {
            logger.error("fault prone when building AuthRequest with :", e);
        }
        return authResponse;
    }
}
