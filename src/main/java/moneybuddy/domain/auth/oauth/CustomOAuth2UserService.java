package moneybuddy.domain.auth.oauth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moneybuddy.domain.auth.dto.OAuthUserInfo;
import moneybuddy.domain.auth.entity.OAuthToken;
import moneybuddy.domain.auth.repository.OauthTokenRepository;
import moneybuddy.domain.user.entity.User;
import moneybuddy.domain.user.repository.UserRepository;
import moneybuddy.global.enums.LoginMethod;
import moneybuddy.global.enums.OAuthProvider;
import moneybuddy.global.enums.UserRole;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final OauthTokenRepository oauthTokenRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        return processOAuth2User(userRequest, oAuth2User);
    }

    public OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // google, kakao, naver
        String userNameAttrName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        Map<String, Object> attributes = oAuth2User.getAttributes();

        OAuthUserInfo userInfo = extractUserInfo(registrationId, attributes);

        // 이메일 없는 경우: 인증은 통과시키되 SuccessHandler에서 분기 처리
        if (userInfo.email() == null) {
            return new DefaultOAuth2User(
                    Collections.emptySet(),
                    attributes,
                    userNameAttrName
            );
        }

        String nickname = generateUniqueNickname(userInfo.name(), userInfo.email());

        User user = userRepository.findByEmail(userInfo.email())
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .email(userInfo.email())
                                .nickname(nickname)
                                .profileImage(userInfo.picture())
                                .role(UserRole.USER)
                                .loginMethod(userInfo.loginMethod())
                                .isDeleted(false)
                                .build()
                ));

        // OAuth Token 저장
        OAuth2AccessToken accessToken = userRequest.getAccessToken();
        Map<String, Object> additionalParams = userRequest.getAdditionalParameters();

        String refreshToken = (String) additionalParams.get("refresh_token");
        OAuthProvider provider = OAuthProvider.valueOf(registrationId.toUpperCase());

        LocalDateTime expiresAt = toLocalDateTime(accessToken.getExpiresAt());

        oauthTokenRepository.findByUserAndProvider(user, provider)
                .ifPresentOrElse(existing -> {
                    existing.update(
                            accessToken.getTokenValue(),
                            refreshToken,
                            expiresAt,
                            String.join(",", accessToken.getScopes())
                    );
                }, () -> {
                    oauthTokenRepository.save(
                            OAuthToken.builder()
                                    .user(user)
                                    .provider(provider)
                                    .accessToken(accessToken.getTokenValue())
                                    .refreshToken(refreshToken)
                                    .expiresAt(expiresAt)
                                    .scope(String.join(",", accessToken.getScopes()))
                                    .build()
                    );
                });


        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
                attributes,
                userNameAttrName
        );
    }


    private OAuthUserInfo extractUserInfo(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) {
            case "google" -> new OAuthUserInfo(
                    (String) attributes.get("email"),
                    (String) attributes.get("name"),
                    (String) attributes.get("picture"),
                    LoginMethod.GOOGLE
            );
            case "kakao" -> {
                @SuppressWarnings("unchecked")
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                @SuppressWarnings("unchecked")
                Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                yield new OAuthUserInfo(
                        (String) kakaoAccount.get("email"),
                        (String) profile.get("nickname"),
                        (String) profile.get("profile_image_url"),
                        LoginMethod.KAKAO
                );
            }
            case "naver" -> {
                @SuppressWarnings("unchecked")
                Map<String, Object> response = (Map<String, Object>) attributes.get("response");
                yield new OAuthUserInfo(
                        (String) response.get("email"),
                        (String) response.get("name"),
                        (String) response.get("profile_image"),
                        LoginMethod.NAVER
                );
            }
            default -> throw new OAuth2AuthenticationException("지원하지 않는 OAuth Provider: " + registrationId);
        };
    }

    private String generateUniqueNickname(String name, String email) {
        String base = (name != null && !name.isBlank())
                ? name
                : (email != null ? "사용자_" + email.split("@")[0] : "사용자");

        String nickname = base;
        int suffix = 1;

        while (userRepository.existsByNickname(nickname)) {
            nickname = base + "_" + suffix++;
            if (suffix > 1000) {
                nickname = base + "_" + UUID.randomUUID().toString().substring(0, 6);
                break;
            }
        }

        return nickname;
    }


    private LocalDateTime toLocalDateTime(Instant instant) {
        return (instant != null) ? LocalDateTime.ofInstant(instant, ZoneId.systemDefault()) : null;
    }
}
