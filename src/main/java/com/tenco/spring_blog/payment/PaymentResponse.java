package com.tenco.spring_blog.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tenco.spring_blog._core.util.MyDateUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

public class PaymentResponse {

    // 결제 내역(포인트 결제) 응답 DTO
    @Data
    public static class ListDto {
        private Long id; // 결제 내역 PK
        private String paymentId; // 결제 번호
        private String pgTxId;
        private int amount;
        private String statusDisplay; // 상태 표시 이름 (결제 완료)
        private String paidAt;

        @Builder
        public ListDto(Payment payment) {
            this.id = payment.getId();
            this.paymentId = payment.getPaymentId();
            this.pgTxId = payment.getPgTxId();
            this.amount = payment.getAmount();
            this.statusDisplay = payment.getStatus().getDisplayName(); // 결제 완료, 결제 대기 , 결제 취소
            this.paidAt = MyDateUtil.timestampFormat(payment.getCreatedAt());
        }
    }

    // 1단계 응답 : 브라우저가 경제창을 띄울 때 사용
    @Data
    public static class PrepareDto {
        private String paymentId; // 서버가 발급한 결제 건 식별자
        private int amount; // 결제 요청 금액
        private String storeId; // 포트원의 상점 ID
        private String channelKey; // 포트원의 채널 키

        @Builder
        public PrepareDto(String paymentId, int amount, String storeId, String channelKey) {
            this.paymentId = paymentId;
            this.amount = amount;
            this.storeId = storeId;
            this.channelKey = channelKey;
        }
    }


    // 3단계 응답 : 충전 결괴
    @Data
    @AllArgsConstructor
    public static class CompleteDto {
        private int amount; // 충전된 포인트
        private int currentPoint; // 충전 후 보유 포인트
    }

    // 포트원 v2 결제 단건 조회 응답 (필요한 필듭만 사용)
    @Data
    public static class ProtOnePayment {
        private String id; // payment_Id
        private String status; // READ,PAID, FAILED, CANCELLED 등 날아옴
        private String pgTxId; // PG사 거래 번호
        private Amount amount;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Amount {
            private Integer total;
        }
    }

}
