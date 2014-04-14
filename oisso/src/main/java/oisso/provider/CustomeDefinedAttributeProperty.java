package oisso.provider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceConfigurationError;
import javax.sql.DataSource;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("userAttributeFactory")
public class CustomeDefinedAttributeProperty implements UserAttributeFactory {

    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(CustomeDefinedAttributeProperty.class);
    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;
    private boolean getInfoFromDB = true;

    @Override
    public Map<String, String> getUserAttribute(String userid) {
        return getInfoFromDB ? getAttributeFromDB(userid) : getRolesFromSpringSecurity(userid);
    }
    /**
     * If no need extra attribute except ROLE,you can get they from Spring.<br>
     * 如果只要角色而不需其它屬性，可直接由Spring取得。
     * @param userid 用戶編號
     * @return 
     */
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
    /**
     * User's attributes from DB.<br>
     * 從資料庫取得用戶的屬性
     * @param userid 用戶編號
     * @return 
     */
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
                    //使用nickname作為userid，您也可額外自定其它的交換屬性
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
