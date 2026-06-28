package com.example.websocket_demo.security.oauth2.userinfo;

import java.util.Map;

public class FacebookOAuth2UserInfo extends OAuth2UserInfo {

    public FacebookOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return (String) attributes.get("id");
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getFirstName() {
        return (String) attributes.get("first_name");
    }

    @Override
    public String getLastName() {
        return (String) attributes.get("last_name");
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getImageUrl() {
        if (attributes.containsKey("picture")) {
            Object pictureObj = attributes.get("picture");
            if (pictureObj instanceof Map) {
                Map<String, Object> pictureObjMap = (Map<String, Object>) pictureObj;
                if (pictureObjMap.containsKey("data")) {
                    Map<String, Object> dataObj = (Map<String, Object>) pictureObjMap.get("data");
                    if (dataObj.containsKey("url")) {
                        return (String) dataObj.get("url");
                    }
                }
            } else if (pictureObj instanceof String) {
                return (String) pictureObj;
            }
        }
        return null;
    }
}
