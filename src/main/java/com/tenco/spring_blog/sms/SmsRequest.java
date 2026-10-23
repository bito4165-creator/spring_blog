package com.tenco.spring_blog.sms;

import com.tenco.spring_blog._core.error.Exception400;
import lombok.Data;

public class SmsRequest {

    // SMS 인증용 DTO
    @Data
    public static class SmsCheckDto {
        private String phoneNumber;
        private String code;

        // 요청시 데이터 검증
        public void validate() {
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                throw new Exception400("휴대폰 번호를 입력해주세요");
            }
        }

        // 코드 인증시 데이터 검증
        public void validateCode() {
            if (code == null || code.trim().isEmpty()) {
                throw new Exception400("코드 번호를 입력해주세요");
            }
        }

    }

}
