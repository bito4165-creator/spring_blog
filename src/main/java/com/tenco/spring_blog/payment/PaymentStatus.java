package com.tenco.spring_blog.payment;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    READY("결제 대기"), // 결제 예정
    PAID("결제 완료"); // 결제 확인 및 포인트 지급 완료

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }
}
