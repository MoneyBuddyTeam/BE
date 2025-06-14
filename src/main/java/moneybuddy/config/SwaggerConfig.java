package moneybuddy.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

  @Value("${server.port:8080}")
  private String serverPort;

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .info(apiInfo())
        .components(new Components());
  }

  private Info apiInfo() {
    return new Info()
        .title("MoneyBuddy API")
        .description("""
                ## 💰 MoneyBuddy 전문가 상담 플랫폼 API
                
                챌린지 기반 경제 상담 서비스의 백엔드 API 문서입니다.
                
                ### 🎯 주요 기능
                - **전문가 관리**: 전문가 목록/상세 조회, 필터링, 검색
                - **카테고리 관리**: 상담 카테고리 조회 및 분류
                - **실시간 상담**: 전문가와 1:1 채팅 상담
                - **미션 시스템**: 맞춤형 경제 챌린지 제공
                
                ### 📋 API 사용 가이드
                1. **인증**: JWT 토큰을 Authorization 헤더에 포함
                2. **페이징**: page(0부터), size(최대 100) 파라미터 사용
                3. **에러**: 표준화된 에러 응답 구조 제공
                
                ### 🔍 카테고리 타입
                - `SPENDING`: 소비관리
                - `SAVINGS`: 저축계획  
                - `INVESTMENT`: 투자전략
                - `DEBT`: 부채관리
                - `ETC`: 기타상담
                """)
        .version("v1.0.0")
        .license(license());
  }

  private License license() {
    return new License()
        .name("MIT License")
        .url("https://opensource.org/licenses/MIT");
  }
}
