package com.tenco.spring_blog.purchase;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.format.DateTimeFormatter;

public class PurchaseResponse {

    @Data
    public static class ListDto {
        private Long id;
        private String title;
        private String boardWriter;
        private int price;
        private String createdAt;


        public ListDto(Purchase purchase) {
            this.id = purchase.getId();
            this.title = purchase.getBoard().getTitle();
            this.boardWriter = purchase.getBoard().getUser().getUsername();
            this.price = purchase.getPrice();

            if (purchase.getCreatedAt() != null) {
                this.createdAt = purchase.getCreatedAt().toLocalDateTime()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM--dd"));
            }
        }
    }
}
