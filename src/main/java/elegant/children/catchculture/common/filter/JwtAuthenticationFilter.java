package elegant.children.catchculture.common.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import elegant.children.catchculture.common.exception.CustomException;
import elegant.children.catchculture.common.exception.ErrorCode;
import elegant.children.catchculture.common.security.JwtTokenProvider;
import elegant.children.catchculture.entity.user.Role;
import elegant.children.catchculture.common.utils.CookieUtils;
import elegant.children.catchculture.service.user.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        final Optional<Cookie> authorization = CookieUtils.getCookie(request, "Authorization");
        try {
            authorization.ifPresentOrElse(
                    cookie -> {
                        String token = CookieUtils.deserialize(cookie.getValue(), String.class);
                        token = jwtTokenProvider.resolveToken(token);
                        if(!jwtTokenProvider.validateToken(token))
                            throw new RuntimeException();
                        final String email = jwtTokenProvider.getEmail(token);
                        final Role role = jwtTokenProvider.getRole(token);
                        final ArrayList<SimpleGrantedAuthority> simpleGrantedAuthorities = new ArrayList<>();
                        simpleGrantedAuthorities.add(new SimpleGrantedAuthority(role.name()));
                        userService.findByEmail(email)
                                .ifPresentOrElse(user -> {
                                    final RememberMeAuthenticationToken rememberMeAuthenticationToken = new RememberMeAuthenticationToken(user.getEmail(), user, simpleGrantedAuthorities);
                                    SecurityContextHolder.getContext().setAuthentication(rememberMeAuthenticationToken);
                                    },
                                        () -> {
                                        throw new RuntimeException();
                                    });

                    },
                    () -> {
                        throw new RuntimeException();
                    }
            );
        } catch (RuntimeException e) {
            log.info("다시 로그인해주세요.");
            CustomException.sendError(objectMapper, response, ErrorCode.LOGIN_FAIL);
            return;
        }
        filterChain.doFilter(request, response);

    }


}
