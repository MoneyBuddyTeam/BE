package moneybuddy.config;


import moneybuddy.util.AES256Util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityUtilConfig {

    @Value("${spring.security.aes-key}")
    private String aesKey;

    @Bean
    public AES256Util aes256Util() {
        return new AES256Util(aesKey);
    }
}
