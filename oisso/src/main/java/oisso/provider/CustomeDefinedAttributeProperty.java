package oisso.provider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceConfigurationError;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("userAttributeFactory")
public class CustomeDefinedAttributeProperty implements UserAttributeFactory {

    private static Logger logger = LoggerFactory.getLogger(CustomeDefinedAttributeProperty.class);
    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;
    private boolean getInfoFromDB = true;

    @Override
    public Map<String, String> getUserAttribute(String userid) {
        return getInfoFromDB ? getAttributeFromDB(userid) : getRolesFromSpringSecurity(userid);
    }

    private Map<String, String> getRolesFromSpringSecurity(String userid) {
        Map<String, String> result = new HashMap<String, String>();
        StringBuilder roles = new StringBuilder();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        //I put userid into nickname,or you can define another exchange extension attribute 
        result.put("nickname", userid);
        result.put("fullname", "Unknown");
        //Extension Exchange Attribute
        result.put("city", "Unknown");
        for (GrantedAuthority a : auth.getAuthorities()) {
            if (roles.length() > 0) {
                roles.append(",").append(a.getAuthority());
            } else {
                roles.append(a.getAuthority());
            }
        }
        //Extension Exchange Attribute
        result.put("roles", roles.toString());
        return result;
    }

    private Map<String, String> getAttributeFromDB(String userid) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            Map<String, String> result = new HashMap<String, String>();
            String sql = "SELECT username,city,authority FROM users as u inner join authorities as a on  a.userid=u.userid WHERE u.userid=?";
            pstmt = conn.prepareCall(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            pstmt.setString(1, userid);
            rs = pstmt.executeQuery();
            String nickname = null;
            StringBuilder roles = new StringBuilder();
            while (rs.next()) {
                if (nickname == null) {
                    //I put userid into nickname,or you can define another exchange extension attribute 
                    nickname = userid;
                    result.put("nickname", nickname);
                    result.put("fullname", rs.getString("username"));
                    //Extension Exchange Attribute
                    result.put("city",rs.getString("city"));
                }
                if (nickname != null) {
                    if (roles.length() > 0) {
                        roles.append(",").append(rs.getString("authority"));
                    } else {
                        roles.append(rs.getString("authority"));
                    }
                }
            }
            //Extension Exchange Attribute
            result.put("roles", roles.toString());
            return result;
        } catch (SQLException e) {
            String errorMsg = String.format("Error prone when fetch  user[%s] info:%s", userid, e.getMessage());
            logger.error(errorMsg, e);
            throw new ServiceConfigurationError(errorMsg, e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException se) {
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException se) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException se) {
                }
            }
        }
    }
}
