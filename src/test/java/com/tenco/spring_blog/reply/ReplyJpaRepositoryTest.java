package com.tenco.spring_blog.reply;

import com.tenco.spring_blog.board.Board;
import com.tenco.spring_blog.user.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ReplyJpaRepositoryTest {

    @Autowired
    private ReplyJpaRepository replyJpaRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("게시글 ID로 댓글 목록을 조회하고 작성자도 함께 조회한다")
    void findAllByBoardIdJoinUser_test() {
        // given: 사용자 등록
        User user1 = User.builder()
                .username("ssar")
                .password("1234")
                .email("ssar@test.com")
                .build();

        User user2 = User.builder()
                .username("cos")
                .password("1234")
                .email("cos@test.com")
                .build();

        em.persist(user1);
        em.persist(user2);

        // given: 게시글 등록
        Board board = Board.builder()
                .title("테스트 게시글")
                .content("테스트 내용")
                .user(user1)
                .build();

        em.persist(board);

        // given: 댓글 등록
        Reply reply1 = Reply.builder()
                .comment("첫 번째 댓글")
                .board(board)
                .user(user1)
                .build();

        Reply reply2 = Reply.builder()
                .comment("두 번째 댓글")
                .board(board)
                .user(user2)
                .build();

        Reply otherReply = Reply.builder()
                .comment("다른 게시글의 댓글")
                .board(board)
                .user(user1)
                .build();

        em.persist(reply1);
        em.persist(reply2);
        em.persist(otherReply);

        em.flush();
        em.clear();

        // when: 게시글 ID로 댓글 조회
        List<Reply> replyList =
                replyJpaRepository.findAllByBoardIdJoinUser(board.getId());

        // then: 댓글 목록 검증
        assertThat(replyList).isNotNull();
        assertThat(replyList).hasSize(3);

        // 댓글 ID 오름차순 정렬 검증
        assertThat(replyList)
                .extracting(Reply::getId)
                .isSorted();

        // 작성자 정보가 함께 조회되었는지 검증
        assertThat(replyList)
                .allSatisfy(reply -> {
                    assertThat(reply.getUser()).isNotNull();
                    assertThat(reply.getUser().getUsername()).isNotBlank();
                });
    }
}