package elegant.children.catchculture.entity.user;

import elegant.children.catchculture.common.security.oauth2.userinfo.GoogleOAuth2UserInfo;
import elegant.children.catchculture.common.security.oauth2.userinfo.KakaoOAuth2UserInfo;
import elegant.children.catchculture.common.security.oauth2.userinfo.NaverOAuth2UserInfo;
import elegant.children.catchculture.common.security.oauth2.userinfo.OAuth2UserInfo;

import java.util.Arrays;
import java.util.Map;


public enum SocialType {

    KAKAO{
            public OAuth2UserInfo toOAuth2UserInfo(Map<String, Object> attributes) {
                return new KakaoOAuth2UserInfo(attributes);
            }
    },
    NAVER{
            public OAuth2UserInfo toOAuth2UserInfo(Map<String, Object> attributes) {
                return new NaverOAuth2UserInfo(attributes);
            }
    },
    GOOGLE {
        public OAuth2UserInfo toOAuth2UserInfo(Map < String, Object > attributes){
            return new GoogleOAuth2UserInfo(attributes);
        }
    };


    public abstract OAuth2UserInfo toOAuth2UserInfo(Map<String, Object> attributes);

    public static SocialType of(String socialType) {
        return Arrays.stream(SocialType.values())
                .filter(type -> type.name().equalsIgnoreCase(socialType))
                .findAny()
                .orElse(null);
    }
}
