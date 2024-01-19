package elegant.children.catchculture.common.security.oauth2;

import elegant.children.catchculture.common.security.oauth2.userinfo.OAuth2UserInfo;
import elegant.children.catchculture.entity.user.Role;
import elegant.children.catchculture.entity.user.SocialType;
import elegant.children.catchculture.entity.user.User;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class OAuthAttributes {

    private String nameAttributeKey;
    private OAuth2UserInfo oAuth2UserInfo;

    public OAuthAttributes(String nameAttributeKey, OAuth2UserInfo oAuth2UserInfo) {
        this.nameAttributeKey = nameAttributeKey;
        this.oAuth2UserInfo = oAuth2UserInfo;
    }

    public static OAuthAttributes of(SocialType socialType,
                                     String userNameAttributeName, Map<String, Object> attributes) {

        return OAuthAttributes.builder()
                .nameAttributeKey(userNameAttributeName)
                .oAuth2UserInfo(socialType.toOAuth2UserInfo(attributes))
                .build();

    }

    public User toEntity(SocialType socialType, OAuth2UserInfo oauth2UserInfo) {
        return User.builder()
                .socialType(socialType)
                .email(oauth2UserInfo.getEmail())
                .nickname(oauth2UserInfo.getNickname())
                .storedFileUrl(oauth2UserInfo.getProfileImageURL())
                .role(Role.USER)
                .build();
    }


}