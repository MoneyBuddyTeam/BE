package moneybuddy.domain.home.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaFallbackController {
    // NOTE: SPA 라우팅 지원을 위해, 모든 비-API 경로를 index.html로 포워딩함.
    // 실제 라우팅은 프론트엔드에서 처리
    @RequestMapping(value = "/{path:[^\\.]*}")  // 확장자 없는 모든 경로
    public String redirect() {
        return "forward:/index.html";
    }
}

