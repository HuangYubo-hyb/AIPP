package com.aiot.demo1.controller;

import com.aiot.demo1.util.CaptchaUtil;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class CaptchaController {
    
    @GetMapping("/captcha")
    public Map<String, String> getCaptcha(HttpSession session) {
        CaptchaUtil.Captcha captcha = CaptchaUtil.generateCaptcha();
        // 将验证码存储在会话中
        session.setAttribute("captcha", captcha.getCode());
        
        Map<String, String> response = new HashMap<>();
        response.put("image", "data:image/png;base64," + captcha.getImage());
        response.put("code", captcha.getCode()); // 仅用于测试，生产环境中不应返回验证码
        
        return response;
    }
}