package elegant.children.catchculture.common.security.oauth2;

import elegant.children.catchculture.common.security.oauth2.userinfo.GoogleOAuth2UserInfo;
import elegant.children.catchculture.common.security.oauth2.userinfo.KakaoOAuth2UserInfo;
import elegant.children.catchculture.common.security.oauth2.userinfo.NaverOAuth2UserInfo;
import elegant.children.catchculture.entity.user.SocialType;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;


class OAuthAttributesTest {

    @Test
    void of() {

        HashMap<String, Object> hashMap = new HashMap<>();
        OAuthAttributes kakaoAccount = OAuthAttributes.of(SocialType.KAKAO, "kakao_account", hashMap);
        OAuthAttributes naverAccount = OAuthAttributes.of(SocialType.NAVER, "naver_account", hashMap);
        OAuthAttributes googleAccount = OAuthAttributes.of(SocialType.GOOGLE, "google_account", hashMap);

        assertEquals(KakaoOAuth2UserInfo.class, kakaoAccount.getOAuth2UserInfo().getClass());
        assertEquals(NaverOAuth2UserInfo.class, naverAccount.getOAuth2UserInfo().getClass());
        assertEquals(GoogleOAuth2UserInfo.class, googleAccount.getOAuth2UserInfo().getClass());
    }



}