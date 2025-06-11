package moneybuddy.domain.auth.oauth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moneybuddy.domain.user.entity.User;
import moneybuddy.domain.user.repository.UserRepository;
import moneybuddy.global.enums.LoginMethod;
import moneybuddy.global.enums.UserRole;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 방어 코드: 이메일이 없으면 예외
        if (!oAuth2User.getAttributes().containsKey("email")) {
            throw new OAuth2AuthenticationException("email 정보 없음");
        }

        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // google, naver 등
        String userNameAttrName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        Map<String, Object> attributes = oAuth2User.getAttributes();
        log.info("OAuth2 Provider: {}", registrationId);
        log.info("UserNameAttributeName: {}", userNameAttrName);
        log.info("OAuth2 Attributes: {}", attributes);

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String picture = (String) attributes.get("picture");

        if (email == null) {
            throw new OAuth2AuthenticationException("이메일 정보가 존재하지 않습니다.");
        }

        // 닉네임 생성 (중복 방지)
        String nickname = generateUniqueNickname(name, email);

        // 기존 사용자 조회 or 신규 생성
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .nickname(nickname)
                            .profileImage(picture)
                            .role(UserRole.USER)
                            .loginMethod(LoginMethod.GOOGLE)
                            .isDeleted(false)
                            .build();
                    return userRepository.save(newUser);
                });

        log.info("로그인 유저 ID: {}, 닉네임: {}, Role: {}", user.getId(), user.getNickname(), user.getRole());

        if (user.getRole() == null) {
            log.error("OAuth2 로그인 실패: 사용자 권한이 null입니다. 사용자: {}", user.getEmail());
            throw new IllegalStateException("OAuth2 로그인 유저 권한 없음");
        }

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
                attributes,
                userNameAttrName
        );
    }

    private String generateUniqueNickname(String name, String email) {
        String base = (name != null && !name.isBlank())
                ? name
                : (email != null ? "구글사용자_" + email.split("@")[0] : "구글사용자");

        String nickname = base;
        int suffix = 1;

        while (userRepository.existsByNickname(nickname)) {
            nickname = base + "_" + suffix;
            suffix++;
            if (suffix > 1000) { // 안전장치
                nickname = base + "_" + UUID.randomUUID().toString().substring(0, 6);
                break;
            }
        }

        return nickname;
    }
}
