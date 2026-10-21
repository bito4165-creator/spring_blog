package com.tenco.spring_blog.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class NaverApiClient {

    // application.yml 의 변수를 가지고 올 때 사용
    @Value("${oauth.naver.client-id}")
    private String naverClientId;
    @Value("${oauth.naver.redirect-uri}")
    private String redirectUri;
    @Value("${oauth.naver.client-secret}")
    private String clientSecret;

    // 인가 요청 주소
    private static String AUTHORIZE_URL = "https://nid.naver.com/oauth2.0/authorize";
    // 엑세스 토큰 요청 주소
    private static String TOKEN_URL = "https://nid.naver.com/oauth2.0/token";
    // 프로필 이미지 요청 주소
    private static String PROFILE_URL = "https://openapi.naver.com/v1/nid/me";
    // 한번 만들면 여러 요청에서 함께 써도 안전하게 설계되어 하나만 만들어 내부사용한다.
    private final RestTemplate restTemplate = new RestTemplate();

    // 1. 네이버 로그인 화면 주소 만들기
    public String getAuthorizeUrl(String state) {
        return AUTHORIZE_URL
                + "?client_id=" + naverClientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code"
                + "&state=" + state;
    }

    // 2. 인가 코드로 엑세스 토큰 요청 (POST, 폼 데이터)
    public UserResponse.naverToken requestAccessToken(String code, String state) {
        // 받은 인가코드로 토큰 요청(POST)
        HttpHeaders headers = new HttpHeaders();

        // Content-Type: application/x-www-form-urlencoded;charset=utf-8
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", naverClientId);
        // 💡 네이버 토큰 요청 명세에 없는 redirect_uri는 제거합니다.
        // params.add("redirect_uri", redirectUri);
        params.add("code", code);
        params.add("state", state);
        // 클라이언트 시크릿 활성화시 반드시 포함
        params.add("client_secret", clientSecret);

        // 바디 + 헤더
        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity(params, headers);

        // 주소, method, HTTP 메서드, 응답 타입
        ResponseEntity<UserResponse.naverToken> response = restTemplate.exchange(
                TOKEN_URL,
                HttpMethod.POST,
                tokenRequest,
                UserResponse.naverToken.class
        );
        log.info("네이버 엑세스 토큰 발급 완료"); // 토큰 값은 로그에 남기지 않는다.
        return response.getBody();
    }

    // 3. 엑세스 토큰으로 사용자 정보 요청 (GET, 인증 헤더)
    public UserResponse.naverUserInfo requestProfile(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity request = new HttpEntity(headers);

        ResponseEntity<UserResponse.naverUserInfo> response = restTemplate.exchange(
                PROFILE_URL, HttpMethod.GET,request,UserResponse.naverUserInfo.class
        );
        return response.getBody();
    }
}
