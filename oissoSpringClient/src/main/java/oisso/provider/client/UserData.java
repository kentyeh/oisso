package oisso.provider.client;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

/**
 * User's principal object.<br>
 * 代表用戶的Principal物件
 *
 * @author Kent Yeh
 */
public class UserData extends User {

    private static final long serialVersionUID = 6275497018781025140L;
    private final String userid;
    private final String city;
    private final String fullname;
    private final String roles;

    public UserData(String userid, String fullname, String city, String roles) {
        //, Collection<? extends GrantedAuthority> authorities
        super(userid, "unused", true, true, true, true,
                AuthorityUtils.commaSeparatedStringToAuthorityList(roles));
        this.city = city;
        this.fullname = fullname;
        this.userid = userid;
        this.roles = roles;
    }

    public String getCity() {
        return city;
    }

    public String getFullname() {
        return fullname;
    }

    public String getUserid() {
        return userid;
    }

    public String getRoles() {
        return roles;
    }

}
