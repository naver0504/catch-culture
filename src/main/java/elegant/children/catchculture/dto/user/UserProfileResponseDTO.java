package elegant.children.catchculture.dto.user;

import elegant.children.catchculture.entity.user.Role;
import elegant.children.catchculture.entity.user.SocialType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserProfileResponseDTO {

    private String storedFileUrl;
    private String nickname;
    private Role role;
    private SocialType socialType;

    public static UserProfileResponseDTO of(final String storedFileUrl, final String nickname, final Role role, final SocialType socialType) {
        return UserProfileResponseDTO.builder()
                .storedFileUrl(storedFileUrl)
                .nickname(nickname)
                .role(role)
                .socialType(socialType)
                .build();
    }
}
