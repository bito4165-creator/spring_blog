package com.tenco.spring_blog.payment;

import com.tenco.spring_blog._core.error.Exception400;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;


public class PaymentRequest {

    // 1.단계 : 결제 건 생성 요청 {amount : 5000}
    @Data
    public static class PrepareDto {
        private Integer amount;

        public void validate()  {
            if (amount == null || amount < 1000) {
                throw new Exception400("최소 충전 금액은 10,00포인트 입니다.");
            }
            if (amount > 100000) {
                throw new Exception400("최대 충전 금액은 100000포인트 입니다");
            }
        }
    }

    // 3단계 : 결제 검증 요청 {paymentId : "point_....}
    @Data
    public static class CompleteDto {
        private String paymentId;

        public void validate()  {
            if (paymentId == null || paymentId.isBlank()) {
                throw new Exception400("결제 건 식별자가 필요합니다.");
            }
        }
    }



}
