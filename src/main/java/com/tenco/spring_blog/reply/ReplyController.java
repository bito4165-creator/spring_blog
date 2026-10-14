package com.tenco.spring_blog.reply;

import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.user.SessionUserDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class ReplyController {

    private final ReplyService replyService;

    // 댓글 작성
    @PostMapping("/reply/save")
    public String save(ReplyRequest.SaveDto saveDto, HttpSession session) {
        // 1. 인증 검사 (로그인 인터셉터가 이미 하고있음)
        // 2. 유효성 검사
        saveDto.validate();
        // 3. 서비스단으로 위임(저장 요청)
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        replyService.save(saveDto, sessionUserDto);
        // 4. 리다이렉트 처리 (PRG 설계)
        return "redirect:/board/" + saveDto.getBoardId();
    }

    // 댓글 삭제
    // 주소 설계 : http://localhost:8080/reply/{id}/delete
    @PostMapping("/reply/{id}/delete")
    public String delete(@PathVariable Long id, @RequestParam(name = "boardId") Long boardId, HttpSession session) {
        // 1. 인증검사
        // 2. 유효성검사
        // 3. 서비스에 위임
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        replyService.deleteById(id, sessionUserDto);
        // 4. 리턴 ? (PRG패턴)
        return "redirect:/board/"+ boardId;
    }

}
