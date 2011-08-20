package oisso.provider;

import java.util.Map;

public interface UserAttributeFactory {
    Map<String,String> getUserAttribute(String userid);
}
