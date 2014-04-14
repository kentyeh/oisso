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
import org.openid4java.message.Parameter;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;
import org.openid4java.message.sreg.SRegMessage;
import org.openid4java.message.sreg.SRegRequest;
import org.openid4java.message.sreg.SRegResponse;
import org.openid4java.server.ServerManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.openid4java.association.AssociationException;
import org.openid4java.message.MessageException;
import org.openid4java.server.ServerException;
import org.springframework.util.StringUtils;

public class CommonUtil {

    private static final Logger logger = LogManager.getLogger(CommonUtil.class);
    private final static String[] OPENID_NS_SREG_FIELDS = new String[]{"nickname", "email", "fullname", "dob", "gender", "postcode", "country", "language", "timezone"};
    private static final Map<String, String> sregSchema = new HashMap<String, String>();

    public static void setSregSchema(Map<String, String> sregSchema) {
        CommonUtil.sregSchema.clear();
        if (sregSchema != null && !sregSchema.isEmpty()) {
            for (String sreg : OPENID_NS_SREG_FIELDS) {
                CommonUtil.sregSchema.put(sreg, sregSchema.get(sreg));
            }
        }
    }

    public static String getContextPath(HttpServletRequest request) {
        StringBuilder pb = new StringBuilder();
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
            for (Map.Entry<String,String> para : additionalParams.entrySet()){
                sb.append(sb.length() == 0 ? "?" : "&").append(para.getKey()).append("=");
                if (StringUtils.hasText(para.getValue())) {
                    sb.append(URLEncoder.encode(para.getValue(), "UTF-8"));
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
            authRequest = AuthRequest.createAuthRequest(requestParameters, serverManager.getRealmVerifier());
            logger.debug("Processing Simple Registration attributes…");
            if (authRequest.hasExtension(SRegMessage.OPENID_NS_SREG)) {
                MessageExtension extensionRequestObject = authRequest.getExtension(SRegMessage.OPENID_NS_SREG);
                if (extensionRequestObject instanceof SRegRequest) {
                    SRegRequest sRegRequest = (SRegRequest) extensionRequestObject;
                    Map<String, String> simpleAttribute = new HashMap<String, String>();
                    for (String sreg : CommonUtil.OPENID_NS_SREG_FIELDS) {
                        if (userAttribute.containsKey(sreg)) {
                            logger.debug("add Attribute {}:{}", sreg, userAttribute.get(sreg).toString());
                            simpleAttribute.put(sreg, userAttribute.get(sreg).toString());
                        }
                    }
                    if (!simpleAttribute.isEmpty()) {
                        SRegResponse sRegResponse = SRegResponse.createSRegResponse(sRegRequest, simpleAttribute);
                        authResponse.addExtension(sRegResponse);
                    }
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
                    for (Object o : parameters.getParameters()) {
                        Parameter param = (Parameter) o;
                        if (param.getKey().startsWith("type.")) {
                            String key = param.getKey().substring(5);
                            if (userAttribute.containsKey(key)) {
                                logger.debug("add extended attribute {}:{}", key, userAttribute.get(key).toString());
                                axData.put(key, userAttribute.get(key).toString());
                                String schema = sregSchema.keySet().contains(key) ? sregSchema.get(key)
                                        : extensionAttrSchema.keySet().contains(key) ? extensionAttrSchema.get(key) : null;
                                if (StringUtils.hasText(schema)) {
                                    fetchResponse.addAttribute(key, schema, userAttribute.get(key).toString());
                                }
                            }
                        }
                    }
                    authResponse.addExtension(fetchResponse);
                } else {
                    logger.error("Cannot continue processing Attribute Exchange (AX) request. The object returned from the AuthRequest (of type {}) claims to be correct, but is not of type {} as expected.",
                            extensionRequestObject.getClass().getName(), AxMessage.class.getName());
                }
            }
            serverManager.sign((AuthSuccess) authResponse);
        } catch (AssociationException e) {
            logger.error("fault prone when building AuthRequest with :", e);
        } catch (MessageException e) {
            logger.error("fault prone when building AuthRequest with :", e);
        } catch (ServerException e) {
            logger.error("fault prone when building AuthRequest with :", e);
        }
        return authResponse;
    }
}
