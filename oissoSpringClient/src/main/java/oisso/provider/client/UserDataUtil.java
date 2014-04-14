package oisso.provider.client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component("userDataUtil")
public class UserDataUtil {

    private static Logger logger = LoggerFactory.getLogger(UserDataUtil.class);
    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    public UserDetails getUserDetails(String identifier) throws SQLException {
        logger.debug("get user details with {}", identifier);
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            String sql = "SELECT * FROM userdata WHERE userid=?";
            pstmt = conn.prepareCall(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            pstmt.setString(1, identifier);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                logger.debug("find user detail with:{},{},{},{}", new String[]{identifier, rs.getString("username"), rs.getString("city"),
                    rs.getString("roles")});
                UserData userdata = new UserData(identifier, rs.getString("username"), rs.getString("city"),
                        rs.getString("roles"));
                return userdata;
            } else {
                logger.debug("find user {} nothintg.", identifier);
                return null;
            }
        } catch (SQLException e) {
            logger.error(String.format("Something wrong::%s", e, e.getMessage()), e);
            throw new SQLException(String.format("find user [%s] with exception:%s", e, e.getMessage()), e);
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
