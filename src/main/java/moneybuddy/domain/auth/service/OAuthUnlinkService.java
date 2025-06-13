package moneybuddy.domain.auth.service;

import lombok.RequiredArgsConstructor;
import moneybuddy.util.AES256Util;
import moneybuddy.domain.auth.entity.OAuthToken;
import moneybuddy.domain.auth.repository.OauthTokenRepository;
import moneybuddy.domain.user.entity.User;
import moneybuddy.domain.user.repository.UserRepository;
import moneybuddy.global.enums.LoginMethod;
import moneybuddy.global.enums.OAuthProvider;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class OAuthUnlinkService {

    private final OauthTokenRepository oauthTokenRepository;
    private final AES256Util aes256Util;
    private final RestTemplate restTemplate;
    private final UserRepository userRepository;

    public void unlink(User user) {
        LoginMethod loginMethod = user.getLoginMethod();
        OAuthProvider provider = OAuthProvider.valueOf(loginMethod.name());

        OAuthToken token = oauthTokenRepository.findByUserAndProvider(user, provider)
                .orElseThrow(() -> new IllegalStateException("OAuth 토큰이 존재하지 않습니다."));

        String accessToken;
        try {
            accessToken = aes256Util.decrypt(token.getAccessToken());
        } catch (Exception e) {
            throw new IllegalStateException("Access Token 복호화에 실패했습니다.", e);
        }

        try {
            switch (provider) {
                case GOOGLE -> unlinkGoogle(accessToken);
                case KAKAO -> unlinkKakao(accessToken);
                case NAVER -> revokeNaver(accessToken); // 폐기만 처리
            }
        } catch (Exception e) {
            throw new IllegalStateException("OAuth 연동 해제 요청 중 오류가 발생했습니다.", e);
        }

        // loginMethod → EMAIL로 초기화
        user.setLoginMethod(LoginMethod.EMAIL);
        userRepository.save(user);

        // OAuthToken 삭제
        oauthTokenRepository.delete(token);

        // 비밀번호가 없다면 후속 조치 필요
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            // 이후 redirect 또는 UI 메시지에서 비밀번호 설정 유도 필요
            // '/users/{id}'로 유저 업데이트 api에 연동시켜주면 좋을듯
            throw new IllegalStateException("비밀번호가 설정되지 않은 계정입니다. 비밀번호를 먼저 설정해 주세요.");
        }
    }

    private void unlinkGoogle(String accessToken) {
        String url = "https://oauth2.googleapis.com/revoke?token=" + accessToken;
        restTemplate.postForLocation(url, null);
    }

    private void unlinkKakao(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<?> request = new HttpEntity<>(headers);

        String url = "https://kapi.kakao.com/v1/user/unlink";
        restTemplate.postForEntity(url, request, String.class);
    }

    private void revokeNaver(String accessToken) {
        String clientId = "YOUR_NAVER_CLIENT_ID";
        String clientSecret = "YOUR_NAVER_CLIENT_SECRET";

        String url = "https://nid.naver.com/oauth2.0/token" +
                "?grant_type=delete" +
                "&client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&access_token=" + accessToken +
                "&service_provider=NAVER";

        restTemplate.getForEntity(url, String.class);
    }
}
