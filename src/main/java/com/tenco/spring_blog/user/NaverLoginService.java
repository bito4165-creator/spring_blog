package com.tenco.spring_blog.user;

import com.tenco.spring_blog._core.error.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverLoginService {
    private final NaverApiClient naverApiClient;
    private final UserService userService;
    // 네이버 로그인 화면 주소
    public String getAuthorizeUrl(String state) {
        return naverApiClient.getAuthorizeUrl(state);
    }

    // 네이버 로그인 : 인가 코드 및 state 검증 -> 토큰 -> 사용자 정보 -> 가입 또는 로그인
    public SessionUserDto login(String code, String state, String sessionState) {
        // state 값 검증
        if (sessionState == null || !sessionState.equals(state)) {
            log.warn("네이버 로그인 보안 경고: state 값이 일치하지 않습니다.");
            throw new UnauthorizedException("유효하지 않은 접근입니다.");
        }

        if (code == null) {
            throw new UnauthorizedException("네이버 로그인이 취소되었습니다.");
        }

        try {
            // 1. 인가코드로 엑세스 토큰 발급 (네이버 서버 통신)
            UserResponse.naverToken naverToken = naverApiClient.requestAccessToken(code,state);

            // 엑세스 토큰으로 사용자 조회
            UserResponse.naverUserInfo naverUserInfo = naverApiClient.requestProfile(naverToken.getAccessToken());

            // 3. 자동 회원가입 또는 로그인 (우리 DB, 트랜잭션은 UserService에서)
            return userService.naverLogin(naverUserInfo);
        } catch (HttpClientErrorException e ) {
            // 네이버 4XX 응답을 한 경우 (인가 코드 재사용, 키 오류 등)
            log.warn("네이버 API 오류 상세 : Status={}, Body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new UnauthorizedException("네이버 로그인에 실패했습니다. 다시 시도해주세요.");
        } catch (ResourceAccessException e) {
            // 네이버 서버에 연결하지 못한 경우(네트워크 오류, 시간 초과)
            log.warn("네이버 서버에 연결 실패 : {} ", e.getMessage());
            throw new UnauthorizedException("네이버 서버에 연결할수 없습니다. 잠시후 . 다시 시도해주세요");
        }

    }
}
