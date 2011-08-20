package oisso.provider.client;

import java.lang.reflect.Field;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.openid.OpenIDAuthenticationProvider;
import org.springframework.security.openid.OpenIDAuthenticationStatus;
import org.springframework.security.openid.OpenIDAuthenticationToken;

@Aspect
public class OpenIDAopAuthenticationProvider {

    protected static Logger logger = LoggerFactory.getLogger(OpenIDAopAuthenticationProvider.class);

    private OpenIDAuthenticationToken extractAuth(JoinPoint jp) {
        for (Object obj : jp.getArgs()) {
            if (obj != null) {
                if (obj instanceof Authentication) {
                    if (obj instanceof OpenIDAuthenticationToken) {
                        return (OpenIDAuthenticationToken) obj;
                    } else {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    @Around("execution(* org.springframework.security.openid.OpenIDAuthenticationProvider.authenticate(..))")
    public Object authenticate(ProceedingJoinPoint pjp) throws Throwable {
        logger.debug("Aop around OpenIDAuthenticationProvider.authenticate method");
        OpenIDAuthenticationToken response = extractAuth(pjp);
        if (response != null) {
            OpenIDAuthenticationStatus status = response.getStatus();
            if (status == OpenIDAuthenticationStatus.SUCCESS) {
                Class<OpenIDAuthenticationProvider> c = OpenIDAuthenticationProvider.class;
                Field f = c.getDeclaredField("userDetailsService");
                f.setAccessible(true);
                Object obj = f.get(pjp.getTarget());
                if (obj != null && obj instanceof AuthenticationUserDetailsService) {
                    AuthenticationUserDetailsService aud = (AuthenticationUserDetailsService) obj;
                    UserDetails userDetails = aud.loadUserDetails(response);
                    return new OpenIDAuthenticationToken(userDetails, userDetails.getAuthorities(),
                            response.getIdentityUrl(), response.getAttributes());
                }
            }
        }
        return pjp.proceed();
    }
}
