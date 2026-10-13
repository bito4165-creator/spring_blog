package com.tenco.spring_blog.board;

import com.tenco.spring_blog.user.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BoardJpaRepositoryTest {

    @Autowired
    private BoardJpaRepository boardJpaRepository;

    @Autowired
    private EntityManager em;

    private User createUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("1234");
        user.setEmail(username + "@test.com");

        em.persist(user);
        return user;
    }

    private Board createBoard(String title, User user) {
        Board board = new Board();
        board.setTitle(title);
        board.setContent("테스트 내용");
        board.setUser(user);

        em.persist(board);
        return board;
    }

    @Test
    @DisplayName("게시글 ID로 조회하면 작성자 정보도 함께 조회한다")
    void findByIdJoinUser_test() {
        // given
        User user = createUser("ssar");
        Board board = createBoard("첫 번째 게시글", user);

        em.flush();
        Long boardId = board.getId();

        // 영속성 컨텍스트를 비워 1차 캐시의 영향을 제거한다.
        em.clear();

        // when
        Optional<Board> result =
                boardJpaRepository.findByIdJoinUser(boardId);

        // then
        assertThat(result).isPresent();

        Board foundBoard = result.get();

        assertThat(foundBoard.getId()).isEqualTo(boardId);
        assertThat(foundBoard.getTitle()).isEqualTo("첫 번째 게시글");
        assertThat(foundBoard.getUser().getUsername()).isEqualTo("ssar");

        // 실제 조회 시 user 연관 객체까지 로딩되었는지 검증한다.
        assertThat(em.getEntityManagerFactory()
                .getPersistenceUnitUtil()
                .isLoaded(foundBoard, "user"))
                .isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 게시글 ID는 빈 Optional을 반환한다")
    void findByIdJoinUser_notFound_test() {
        // given
        Long invalidId = -1L;

        // when
        Optional<Board> result =
                boardJpaRepository.findByIdJoinUser(invalidId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("전체 게시글을 작성자 정보와 함께 ID 내림차순으로 조회한다")
    void findAllJoinUser_test() {
        // given
        User user1 = createUser("ssar");
        User user2 = createUser("cos");

        Board board1 = createBoard("첫 번째 게시글", user1);
        Board board2 = createBoard("두 번째 게시글", user2);
        Board board3 = createBoard("세 번째 게시글", user1);

        em.flush();
        em.clear();

        // when
        List<Board> boards = boardJpaRepository.findAllJoinUser();

        // then
        assertThat(boards).hasSize(3);

        assertThat(boards)
                .extracting(Board::getId)
                .containsExactly(
                        board3.getId(),
                        board2.getId(),
                        board1.getId()
                );

        assertThat(boards)
                .allSatisfy(board -> {
                    assertThat(board.getUser()).isNotNull();

                    assertThat(em.getEntityManagerFactory()
                            .getPersistenceUnitUtil()
                            .isLoaded(board, "user"))
                            .isTrue();
                });
    }
}