package com.tenco.spring_blog._core.util;

import java.security.SecureRandom;

public class SmsUtil {
    // 난수 생성기
    private static final SecureRandom RANDOM = new SecureRandom();

    // 6자리 난수 생성
    public static String generateCode() {
        int code = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(code);
    }
}
