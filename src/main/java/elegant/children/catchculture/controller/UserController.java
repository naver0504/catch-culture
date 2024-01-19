package elegant.children.catchculture.controller;

import elegant.children.catchculture.common.constant.Classification;
import elegant.children.catchculture.dto.culturalEvent.response.CulturalEventListResponseDTO;
import elegant.children.catchculture.dto.user.UserProfileResponseDTO;
import elegant.children.catchculture.entity.culturalevent.Category;
import elegant.children.catchculture.entity.user.User;
import elegant.children.catchculture.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponseDTO> getUserProfile(final @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(UserProfileResponseDTO.of(user.getStoredFileUrl(), user.getNickname(), user.getRole(),user.getSocialType()));
    }

    @PatchMapping("/profile/nickname")
    public void updateUserProfile(final @AuthenticationPrincipal User user, @RequestParam String nickName) {
        userService.updateUserNickname(user, nickName);
    }

    @GetMapping("/cultural-event")
    public ResponseEntity<Page<CulturalEventListResponseDTO>> getCulturalEventList(final @AuthenticationPrincipal User user,
                                                                                   final @RequestParam(required = false) List<Category> category,
                                                                                   final @RequestParam(required = false, defaultValue = "0") int offset,
                                                                                   final @RequestParam(required = false, defaultValue = "LIKE") Classification classification) {

        return ResponseEntity.ok(userService.getCulturalEventListWithUser(user, offset, category, classification));
    }

    @GetMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response, final @AuthenticationPrincipal User user) {
        userService.logout(request, response, user);
    }

    @GetMapping("sign-out")
    public void singOut(HttpServletRequest request, HttpServletResponse response, final @AuthenticationPrincipal User user) {
        userService.singOut(request, response, user);
    }

    @PatchMapping(value = "/profile/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void updateUserProfileImage(final @AuthenticationPrincipal User user, final @RequestParam MultipartFile file) {
        userService.updateUserProfileImage(user, file);
    }


}
