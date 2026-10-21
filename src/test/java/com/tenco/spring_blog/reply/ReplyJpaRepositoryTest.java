package com.tenco.spring_blog.reply;

import com.tenco.spring_blog.board.Board; // 확인 필요: Board 엔티티 패키지
import com.tenco.spring_blog.user.OAuthProvider;
import com.tenco.spring_blog.user.User; // 확인 필요: User 엔티티 패키지
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest; // Spring Boot 4 경로

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ReplyJpaRepositoryTest {

    @Autowired
    private ReplyJpaRepository replyJpaRepository;

    @Autowired
    private EntityManager em;

    // 테스트용 User 생성
    private User createUser(String username) {
        User user = User.builder()
                .username(username) // 확인 필요: User에 username 필드와 빌더가 있다고 가정
                .password("test-password") // 확인 필요: User에 password 필드가 있다고 가정
                .email(username + "@test.com") // 확인 필요: User에 email 필드가 있다고 가정
                .provider(OAuthProvider.LOCAL) // 확인 필요: 실제 Provider enum 값 확인
                .build();

        em.persist(user);
        return user;
    }

    // 테스트용 Board 생성
    private Board createBoard(String title, User user) {
        Board board = Board.builder()
                .title(title) // 확인 필요: Board에 title 필드와 빌더가 있다고 가정
                .content("테스트 게시글 내용") // 확인 필요: Board에 content 필드가 있다고 가정
                .user(user) // 확인 필요: Board에 User 연관관계가 있다고 가정
                .build();

        em.persist(board);
        return board;
    }

    // 테스트용 Reply 생성
    private Reply createReply(String comment, Board board, User user) {
        Reply reply = Reply.builder()
                .comment(comment) // 확인 필요: Reply의 댓글 내용 필드가 content라고 가정
                .board(board) // 확인 필요: Reply에 Board 연관관계가 있다고 가정
                .user(user) // 확인 필요: Reply에 User 연관관계가 있다고 가정
                .build();

        em.persist(reply);
        return reply;
    }

    @Test
    void findAllByBoardIdJoinUser_댓글이있는_게시글_테스트() {
        // given: 사용자와 게시글을 만들고 댓글 2개를 저장한다.
        User user = createUser("user1");
        Board board = createBoard("첫 번째 게시글", user);

        createReply("첫 번째 댓글", board, user);
        createReply("두 번째 댓글", board, user);

        em.flush();
        em.clear();

        // when: 게시글 ID로 댓글 목록을 조회한다.
        List<Reply> replies =
                replyJpaRepository.findAllByBoardIdJoinUser(board.getId());

        // then: 해당 게시글의 댓글 2개가 조회된다.
        assertThat(replies).hasSize(2);
    }

    @Test
    void findAllByBoardIdJoinUser_오래된순서_조회_테스트() {
        // given: 동일한 게시글에 댓글 3개를 차례로 저장한다.
        User user = createUser("user2");
        Board board = createBoard("댓글 순서 테스트", user);

        Reply first = createReply("첫 번째 댓글", board, user);
        Reply second = createReply("두 번째 댓글", board, user);
        Reply third = createReply("세 번째 댓글", board, user);

        em.flush();
        Long firstId = first.getId();
        Long secondId = second.getId();
        Long thirdId = third.getId();
        em.clear();

        // when: 댓글 목록을 조회한다.
        List<Reply> replies =
                replyJpaRepository.findAllByBoardIdJoinUser(board.getId());

        // then: 댓글 ID가 오름차순으로 조회된다.
        assertThat(replies)
                .extracting(Reply::getId)
                .containsExactly(firstId, secondId, thirdId);
    }

    @Test
    void findAllByBoardIdJoinUser_다른게시글_댓글제외_테스트() {
        // given: 두 게시글에 댓글을 각각 저장한다.
        User user = createUser("user3");
        Board firstBoard = createBoard("첫 번째 게시글", user);
        Board secondBoard = createBoard("두 번째 게시글", user);

        createReply("첫 번째 게시글 댓글", firstBoard, user);
        createReply("두 번째 게시글 댓글", secondBoard, user);

        em.flush();
        em.clear();

        // when: 첫 번째 게시글의 댓글만 조회한다.
        List<Reply> replies =
                replyJpaRepository.findAllByBoardIdJoinUser(firstBoard.getId());

        // then: 첫 번째 게시글의 댓글만 반환된다.
        assertThat(replies)
                .hasSize(1)
                .allSatisfy(reply ->
                        assertThat(reply.getBoard().getId())
                                .isEqualTo(firstBoard.getId()));
    }

    @Test
    void findAllByBoardIdJoinUser_댓글없는_게시글_테스트() {
        // given: 게시글만 만들고 댓글은 저장하지 않는다.
        User user = createUser("user4");
        Board board = createBoard("댓글 없는 게시글", user);

        em.flush();
        em.clear();

        // when: 댓글 목록을 조회한다.
        List<Reply> replies =
                replyJpaRepository.findAllByBoardIdJoinUser(board.getId());

        // then: 빈 리스트가 반환된다.
        assertThat(replies).isEmpty();
    }

    @Test
    void findAllByBoardIdJoinUser_작성자_함께로딩_테스트() {
        // given: 작성자와 게시글, 댓글을 저장한 뒤 영속성 컨텍스트를 비운다.
        User user = createUser("user5");
        Board board = createBoard("작성자 로딩 테스트", user);
        createReply("작성자 확인용 댓글", board, user);

        em.flush();
        em.clear();

        PersistenceUnitUtil persistenceUnitUtil =
                em.getEntityManagerFactory().getPersistenceUnitUtil();

        // when: JOIN FETCH 쿼리로 댓글을 조회한다.
        List<Reply> replies =
                replyJpaRepository.findAllByBoardIdJoinUser(board.getId());

        // then: 조회된 댓글의 user 연관 엔티티가 로딩되어 있다.
        assertThat(replies).hasSize(1);
        assertThat(persistenceUnitUtil.isLoaded(replies.get(0), "user"))
                .isTrue();
    }

    @Test
    void deleteByBoardId_해당게시글_댓글삭제_테스트() {
        // given: 한 게시글에 댓글 2개를 저장한다.
        User user = createUser("user6");
        Board board = createBoard("댓글 삭제 테스트", user);

        createReply("삭제할 댓글 1", board, user);
        createReply("삭제할 댓글 2", board, user);

        em.flush();

        // when: 해당 게시글에 연결된 댓글을 일괄 삭제한다.
        replyJpaRepository.deleteByBoardId(board.getId());

        em.flush();
        em.clear();

        // then: 해당 게시글의 댓글이 모두 삭제된다.
        List<Reply> replies =
                replyJpaRepository.findAllByBoardIdJoinUser(board.getId());

        assertThat(replies).isEmpty();
    }

    @Test
    void deleteByBoardId_다른게시글_댓글유지_테스트() {
        // given: 두 게시글에 각각 댓글을 저장한다.
        User user = createUser("user7");
        Board targetBoard = createBoard("삭제 대상 게시글", user);
        Board otherBoard = createBoard("유지할 게시글", user);

        createReply("삭제 대상 댓글", targetBoard, user);
        createReply("유지할 댓글", otherBoard, user);

        em.flush();

        // when: 삭제 대상 게시글의 댓글만 일괄 삭제한다.
        replyJpaRepository.deleteByBoardId(targetBoard.getId());

        em.flush();
        em.clear();

        // then: 다른 게시글의 댓글은 남아 있다.
        List<Reply> remainingReplies =
                replyJpaRepository.findAllByBoardIdJoinUser(otherBoard.getId());

        assertThat(remainingReplies).hasSize(1);
        assertThat(remainingReplies.get(0).getComment())
                .isEqualTo("유지할 댓글"); // 확인 필요: Reply의 내용 getter가 getContent라고 가정
    }

    @Test
    void deleteByBoardId_댓글없는_게시글_테스트() {
        // given: 댓글이 없는 게시글을 만든다.
        User user = createUser("user8");
        Board board = createBoard("댓글이 없는 게시글", user);

        em.flush();

        // when: 해당 게시글의 댓글 삭제 쿼리를 실행한다.
        replyJpaRepository.deleteByBoardId(board.getId());

        em.flush();
        em.clear();

        // then: 댓글이 없는 상태가 그대로 유지된다.
        List<Reply> replies =
                replyJpaRepository.findAllByBoardIdJoinUser(board.getId());

        assertThat(replies).isEmpty();
    }
}