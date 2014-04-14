package oisso.provider.client;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.openid.OpenIDAttribute;
import org.springframework.security.openid.OpenIDAuthenticationToken;
import org.springframework.stereotype.Service;

@Service("openIdUserService")
public class OpenIdUserService implements UserDetailsService, AuthenticationUserDetailsService<OpenIDAuthenticationToken> {

    private static final Logger logger = LogManager.getLogger(OpenIdUserService.class);
    @Autowired
    @Qualifier("userDataUtil")
    private UserDataUtil userDataUtil;

    @Autowired
    @Qualifier("messageAccessor")
    MessageSourceAccessor messageAccessor;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, org.springframework.dao.DataAccessException {
        throw new UsernameNotFoundException(messageAccessor.getMessage("openidService.notSupported31"));
    }

    @Override
    public UserDetails loadUserDetails(OpenIDAuthenticationToken token) throws UsernameNotFoundException {
        logger.debug("Load user details with identifier:{}", token.getPrincipal().toString());
        String userid = null;
        String fullname = null;
        String city = null;
        String roles = null;
        List<OpenIDAttribute> attributes = token.getAttributes();
        for (OpenIDAttribute attribute : attributes) {
            if (attribute.getName().equals("nickname")) {
                userid = attribute.getValues().get(0);
            } else if (attribute.getName().equals("fullname")) {
                fullname = attribute.getValues().get(0);
            } else if (attribute.getName().equals("city")) {
                city = attribute.getValues().get(0);
            } else if (attribute.getName().equals("roles")) {
                roles = attribute.getValues().get(0);
            }
        }
        if (userid == null) {
            throw new UsernameNotFoundException(messageAccessor.getMessage("openidService.notSupported31", fullname));
        } else {
            try {
                UserDetails userdata = userDataUtil.getUserDetails(userid);
                if (userdata == null) {
                    throw new UsernameNotFoundException(messageAccessor.getMessage("openidService.userNotConfigured", fullname));
                } else {
                    return userdata;
                }
            } catch (SQLException ex) {
                logger.error(ex.getMessage(), ex);
                throw new UsernameNotFoundException(ex.getMessage(), ex);
            }
        }
    }
}
