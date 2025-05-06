package com.ntn.configs;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;


// Service của OAuth2
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        // Lấy thông tin từ provider
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // Lưu thông tin vào attributes để OAuth2LoginSuccessHandler có thể truy cập sau này
        Map<String, Object> attributes = new HashMap<>(oauth2User.getAttributes());

        // Thêm provider ID vào attributes
        attributes.put("provider", registrationId);

        // Xử lý thông tin theo provider
        if ("google".equals(registrationId)) {
            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");
            String picture = oauth2User.getAttribute("picture");

            // Thêm flag để kiểm tra xem có cần thông tin bổ sung không
            attributes.put("needAdditionalInfo", false);

        } else if ("facebook".equals(registrationId)) {
            String name = oauth2User.getAttribute("name");
            String id = oauth2User.getAttribute("id");
            String picture = "https://graph.facebook.com/" + id + "/picture?type=large";

            // Cập nhật picture URL
            attributes.put("picture", picture);

            // Đối với Facebook, luôn yêu cầu thông tin bổ sung (email)
            attributes.put("needAdditionalInfo", true);

        }

        // Tạo một DefaultOAuth2User mới với các attributes đã được cập nhật
        Collection<? extends GrantedAuthority> authorities = oauth2User.getAuthorities();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        return new DefaultOAuth2User(authorities, attributes, userNameAttributeName);
    }
}
