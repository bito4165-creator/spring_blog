package com.tenco.spring_blog.sms;

import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.user.SessionUserDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor // DI
@Slf4j
@Controller
public class SmsController {
    // 휴대폰 인증 페이지 이동
    @GetMapping("/user/sms")
    public String smsRegisterPage(HttpSession session) {
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        if (sessionUserDto == null) {
            return "redirect:/";
        }
        return "user/sms";
    }
}
