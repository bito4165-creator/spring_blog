package com.tenco.spring_blog.user;

import com.tenco.spring_blog._core.error.UnauthorizedException;
import com.tenco.spring_blog._core.util.Define;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@RequiredArgsConstructor // DI
@Slf4j
@Controller // IoC (제어의 역전) 싱글톤 패턴으로 관리 됨.
public class UserController {

    private final UserService userService;
    private final KakaoLoginService kakaoLoginService;
    private final NaverLoginService naverLoginService;

    @GetMapping("/kakao-login")
    public String kakaoLogin() {
        return "redirect:" + kakaoLoginService.getAuthorizeUrl();
    }

    // 리다이렉트로 들어와서 카카오 서버가 code 값을 동적으로 꼽아주는 URL 입니다
    @GetMapping("/oauth/kakao")
    public String kakaoCallback(@RequestParam(name = "code", required = false) String code, HttpSession session) {
       SessionUserDto sessionUserDto = kakaoLoginService.login(code);
       session.setAttribute(Define.SESSION_USER, sessionUserDto);
       return "redirect:/";
    }

    @GetMapping("naver-login")
    public String naverLogin(HttpSession session) {
        String state = UUID.randomUUID().toString();

        session.setAttribute("naverState", state);

        return "redirect:" + naverLoginService.getAuthorizeUrl(state);
    }

    @GetMapping("/login/oauth2/code/naver")
    public String naverCallback(@RequestParam(value = "code", required = false)String code,
                                @RequestParam(value = "state", required = false)String state,
                                HttpSession session) {
        // 세션에 저장해둔 state 가져오기
        String sessionState = (String) session.getAttribute("naverState");

        // 사용이 끝난 state는 세션에서 제거
        session.removeAttribute("naverState");

        // 네이버 로그인 서비스 호출 (code,state)
        SessionUserDto sessionUserDto = naverLoginService.login(code, state, sessionState);

        // 세션에 사용자 정보 저장
        session.setAttribute(Define.SESSION_USER, sessionUserDto);

        return "redirect:/";
    }


    // 프로필 삭제
    @PostMapping("/user/profile-image/delete")
    public String deleteProfileImage(HttpSession session) {
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        userService.deleteProfileImage(sessionUserDto.getId());
        return "redirect:/user/detail";
    }


    // GET http://localhost:8080/user/detail
    @GetMapping("user/detail")
    public String detail(Model model, HttpSession session) {
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        UserResponse.DetailDto user = userService.findDetail(sessionUserDto.getId());
        model.addAttribute("user", user);
        return "user/detail";
    }

    // GET http://localhost:8080/join
    @GetMapping("/join")
    public String joinForm() {
        return "user/join-form";
    }

    // POST http://localhost:8080/join
    @PostMapping("/join")
    public String join(UserRequest.JoinDto joinDto, Model model) {
        joinDto.validate();
        userService.join(joinDto);
        return "redirect:/login";
    }

    // GET http://localhost:8080/login
    @GetMapping("/login")
    public String loginForm() {
        return "user/login-form";
    }

    // POST http://localhost:8080/login
    // 로그인 처리 (예외적으로 POTS 요청)
    @PostMapping("/login")
    public String login(UserRequest.LoginDto loginDto, HttpSession session, Model model) {
        loginDto.validate();
        SessionUserDto sessionUserDto = userService.login(loginDto);
        session.setAttribute(Define.SESSION_USER, sessionUserDto);
        return "redirect:/";
    }

    // GET http://localhost:8080/user/update
    @GetMapping("/user/update")
    public String updateForm(Model model, HttpSession session) {
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        UserResponse.updateFormDto userUpdateFormDto = userService.findUpdateForm(sessionUserDto.getId());
        model.addAttribute("user", userUpdateFormDto);
        return "user/update-form";
    }

    // POST http://localhost:8080/user/update
    @PostMapping("/user/update")
    public String update(UserRequest.UpdateDto updateDto, Model model, HttpSession session) {
        updateDto.validate();
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        SessionUserDto updateSessionUserDto = userService.updateById(sessionUserDto.getId(), updateDto);
        // 세션 동기화 처리
        session.setAttribute(Define.SESSION_USER, updateSessionUserDto);
        return "redirect:/user/detail";
    }

    // GET http://localhost:8080/logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
