package com.tenco.spring_blog._core.interceptor;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception403;
import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.user.SessionUserDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminInterceptor implements HandlerInterceptor {
    // 컨트롤러를 들어가기전에 확인하면 되기때문에 preHandle 사용
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 로그인 여부 확인을 위해서 세션 가져오기
        HttpSession session = request.getSession(false);
        // 로그인하지 않은 경우 예외처리
        if (session == null || session.getAttribute(Define.SESSION_USER) == null) {
            throw new Exception400("로그인해주세요");
        }
        // 로그인된 유저의 정보를 가져오기위한 세션 준비
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        // 관리자권환 없는 경우 예외 처리
        if (!sessionUserDto.isAdmin()) {
            throw new Exception403("관리자 권한이 없습니다.");
        }
        // 전부 통과
        return true;
    }
}
