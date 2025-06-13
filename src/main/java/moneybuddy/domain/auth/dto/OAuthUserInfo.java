package moneybuddy.domain.auth.dto;

import moneybuddy.global.enums.LoginMethod;

public record OAuthUserInfo(
        String email,
        String name,
        String picture,
        LoginMethod loginMethod
) {
}
