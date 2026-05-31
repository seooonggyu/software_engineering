package com.project.software_engineering.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AITestController {

    @GetMapping("/ai-test")
    public String aiTestPage() {
        return "ai-test"; // src/main/resources/templates/ai-test.html 을 렌더링
    }
}
