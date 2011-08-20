package oisso.provider.client;

import java.security.Principal;
import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class OpenIdClient {

    private static Logger logger = LoggerFactory.getLogger(OpenIdClient.class);
    @Autowired
    @Qualifier("userDataUtil")
    private UserDataUtil userDataUtil;
    @Autowired
    @Qualifier("messageAccessor")
    MessageSourceAccessor messageAccessor;

    @RequestMapping("/userinfo")
    public String userinfo(HttpServletRequest request, Principal principal) throws SQLException {
        logger.debug("Before fetch userinfo with {}", principal);
        request.setAttribute("userinfo", userDataUtil.getUserDetails(principal.getName()));
        return "userinfo";
    }

    @ExceptionHandler({SQLException.class})
    public String error(Exception exception, HttpServletRequest request) {
        logger.error(exception.getMessage(), exception);
        request.setAttribute("exception", exception);
        return "error";
    }
}
