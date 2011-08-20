package oisso.provider.client;

import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.openid.OpenIDAttribute;
import org.springframework.security.openid.OpenIDAuthenticationToken;
import org.springframework.stereotype.Service;

@Service("openIdUserService")
public class OpenIdUserService implements UserDetailsService, AuthenticationUserDetailsService {

    private static Logger logger = LoggerFactory.getLogger(OpenIdUserService.class);
    @Autowired
    @Qualifier("userDataUtil")
    private UserDataUtil userDataUtil;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, org.springframework.dao.DataAccessException {
        logger.debug("Load user by username:{}", username);
        UserDetails userData;
        try {
            userData = userDataUtil.getUserDetails(username);
        } catch (SQLException ex) {
            logger.error(ex.getMessage(), ex);
            throw new DataAccessException(ex.getMessage(), ex) {
            };
        }
        if (userData == null) {
            throw new UsernameNotFoundException(username);
        }
        return userData;
    }

    @Override
    public UserDetails loadUserDetails(Authentication token) throws UsernameNotFoundException {
        logger.debug("Load user details with identifier:{}", token.getPrincipal().toString());
        OpenIDAuthenticationToken otoken = (OpenIDAuthenticationToken) token;
        String nickname = null;;
        String fullname = null;
        String city = null;
        String roles = null;
        List<OpenIDAttribute> attributes = otoken.getAttributes();
        for (OpenIDAttribute attribute : attributes) {
            if (attribute.getName().equals("nickname")) {
                nickname = attribute.getValues().get(0);
            } else if (attribute.getName().equals("fullname")) {
                fullname = attribute.getValues().get(0);
            } else if (attribute.getName().equals("city")) {
                city = attribute.getValues().get(0);
            } else if (attribute.getName().equals("roles")) {
                roles = attribute.getValues().get(0);
            }
        }
        try {
            return userDataUtil.inserUser(nickname, fullname, city, roles);
        } catch (SQLException ex) {
            logger.error(ex.getMessage(), ex);
            throw new UsernameNotFoundException(ex.getMessage(), ex);
        }
    }
}
