package oisso.provider.client;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class UserData extends User {
    private String userid;
    private String city;
    private String fullname;

    public UserData(String userid, String fullname, String city, Collection<? extends GrantedAuthority> authorities) {
        super(userid, "unused", true, true, true, true, authorities);
        this.city = city;
        this.fullname = fullname;
        this.userid = userid;
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
    
}
