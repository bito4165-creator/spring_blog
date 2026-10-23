package com.tenco.spring_blog.sms;


import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception500;
import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog._core.util.SmsUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.LocalDateTime;
// Solapi SDK 임포트
import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.exception.SolapiMessageNotReceivedException;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;

@Slf4j
@RequiredArgsConstructor
@Service
public class SmsService {
    @Value("${solapi.api-key}")
    private String apiKey;
    @Value("${solapi.api-secret}")
    private String apiSecret;
    @Value("${solapi.sender}")
    private String senderNumber;

    private record SmsCode(String code, LocalDateTime expiresAt) implements Serializable {
        boolean isExpired() {
            return LocalDateTime.now().isAfter(expiresAt);
        }
    }
    // 1. 인증번호 발송
    public void sendVerificationCode(String phoneNumber, HttpSession session) {
        String code = SmsUtil.generateCode();

        try {
            // Solapi 클라이언트 생성
            DefaultMessageService messageService = SolapiClient.INSTANCE.createInstance(apiKey, apiSecret);
            // 메세지 객체 생성
            Message message = new Message();
            message.setFrom(senderNumber);
            message.setTo(phoneNumber);
            message.setText("[스프링블로그] 인증번호는 " + code + "입니다.");
            // 메세지 발송 요청
            messageService.send(message);
            log.info("문자 전송 성공 - 수신번호 {}", phoneNumber);
        } catch (SolapiMessageNotReceivedException exception) {
            log.error("전송 실패 목록 : {}", exception.getFailedMessageList());
            throw new Exception500("문자 발송 실패" + exception.getMessage());
        } catch (Exception exception) {
            log.error("오류 발생 : {}", exception.getMessage());
            throw new Exception500("문자 발송중 오류 발생");
        }
        session.setAttribute(Define.SMS_CODE_PREFIX + phoneNumber, new SmsCode(code, LocalDateTime.now().plusMinutes(Define.CODE_EXPIRE_MINUTES)));

    }

    // 2. 인증 번호 확인 : 일치하면 true
    public boolean verifyCode(String phoneNumber, String code, HttpSession session) {
        String key = Define.SMS_CODE_PREFIX + phoneNumber;
        SmsCode savedSmsCode = (SmsCode) session.getAttribute(key);

        if (savedSmsCode == null) {
            throw new Exception400("인증번호를 입력하세요");
        }
        if (savedSmsCode.isExpired()) {
            session.removeAttribute(key);
            throw new Exception400("인증번호가 만료되었습니다. 다시 요청해주세요.");
        }
        if (!savedSmsCode.code.equals(code)) {
            throw new Exception400("인증번호가 일치하지 않습니다.");
        }

        // 성공 : 인증번호 삭제
        session.removeAttribute(key);

        // 인증 마친 폰번호 객체에 저장
        session.setAttribute(Define.VERIFIED_PHONE, phoneNumber);
        return true;
    }
}
