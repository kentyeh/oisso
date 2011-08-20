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
                        AuthorityUtils.commaSeparatedStringToAuthorityList(rs.getString("roles")));
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

    public UserDetails inserUser(String userid, String fullname, String city, String roles) throws SQLException {
        logger.debug("insert user with({},{},{},{})", new String[]{userid, fullname, city, roles});
        Connection conn = null;
        PreparedStatement pstmt = null, pstmt2 = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            String sql = "SELECT * FROM userdata WHERE userid=?";
            pstmt = null;// conn.prepareCall(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            //pstmt.setString(1, userid);
            //rs = pstmt.executeQuery();
            if (rs != null && rs.next()) {
                sql = "UPDATE userdata set username=?,city=?,roles=?where userid=?";
                pstmt2 = conn.prepareCall(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                pstmt2.setString(1, fullname);
                pstmt2.setString(2, city);
                pstmt2.setString(3, roles);
                pstmt2.setString(4, userid);
                assert pstmt2.executeUpdate() == 1 : "Update user:" + userid + " failed";
                logger.debug("DB Data updated success");
            } else {
                sql = "insert into userdata (userid,username,city,roles) values(?,?,?,?)";
                pstmt2 = conn.prepareCall(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                pstmt2.setString(1, userid);
                pstmt2.setString(2, fullname);
                pstmt2.setString(3, city);
                pstmt2.setString(4, roles);
                assert pstmt2.executeUpdate() == 1 : "Update user:" + userid + " failed";
                logger.debug("DB Data inserted success");
            }
            UserData userdata = new UserData(userid, fullname, city, AuthorityUtils.commaSeparatedStringToAuthorityList(roles));
            return userdata;
        } catch (SQLException e) {
            logger.error(String.format("Something wrong::%s", e, e.getMessage()), e);
            throw new SQLException(String.format("find user [%s] with exception:%s", e, e.getMessage()), e);
        } finally {
            if (pstmt2 != null) {
                try {
                    pstmt2.close();
                } catch (SQLException se) {
                }
            }
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
