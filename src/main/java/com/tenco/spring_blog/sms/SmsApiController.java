package com.tenco.spring_blog.sms;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.user.SessionUserDto;
import com.tenco.spring_blog.user.UserRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
public class SmsApiController {
    private final SmsService smsService;


    // POST/api/sms/send
    @PostMapping("/api/sms/send")
    public ResponseEntity<?> sendSmsCode(@RequestBody SmsRequest.SmsCheckDto smsCheckDto, HttpSession session) {
        smsCheckDto.validate();
        smsService.sendVerificationCode(smsCheckDto.getPhoneNumber(), session);
        return ResponseEntity.ok(Map.of("message", "인증번호가 발송되었습니다."));
    }

    @PostMapping("/api/sms/verify")
    public ResponseEntity<?> verifySmsCode(@RequestBody SmsRequest.SmsCheckDto smsCheckDto, HttpSession session) {
        smsCheckDto.validateCode();
        boolean verified = smsService.verifyCode(smsCheckDto.getPhoneNumber(), smsCheckDto.getCode(), session);
        if (!verified) {
            return ResponseEntity.badRequest().body(Map.of("message", "인증번호가 일치하지 않습니다"));
        }
        return ResponseEntity.ok().body(Map.of("message", "인증되었습니다."));
    }


    // 작업중 예외처리 확인
    // ==== 이 컨트롤러에서 발생한 예외는 JSOn으로 별도 응답 처리 ===
    @ExceptionHandler(Exception400.class)
    public ResponseEntity<Map<String, String>> handleEx400(Exception400 e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }

    // 메일 발송 실패 등 그 밖의 오류 (500)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleEx500(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "메일 발송중 오류가 발생했습니다."));
    }

}
