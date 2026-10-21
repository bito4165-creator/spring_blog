
package com.tenco.spring_blog.board;

import com.tenco.spring_blog.user.Provider; // 확인 필요: Provider enum의 실제 패키지와 클래스명
import com.tenco.spring_blog.user.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BoardJpaRepositoryTest {

    @Autowired
    private BoardJpaRepository boardJpaRepository;

    @Autowired
    private EntityManager em;

    // given 데이터 생성: 필수 필드를 포함한 사용자를 저장한다.
    private User createUser(String username) {
        User user = User.builder()
                .username(username)
                .password("encoded-password")
                .email(username + "@test.com")
                .provider(Provider.LOCAL) // 확인 필요: 실제 enum 타입과 LOCAL 상수
                .build();

        em.persist(user);
        return user;
    }

    // given 데이터 생성: 작성자와 연결된 게시글을 저장한다.
    private Board createBoard(String title, String content, User user) {
        Board board = Board.builder()
                .title(title)
                .content(content)
                .user(user)
                .build(); // 확인 필요: Board 빌더와 필드명이 실제 코드와 일치하는지

        em.persist(board);
        return board;
    }

    @Test
    @DisplayName("게시글 ID로 조회하면 해당 게시글이 반환된다")
    void findByIdJoinUser_존재하는_게시글_테스트() {
        // given: 작성자와 게시글을 저장하고 게시글 ID를 보관한다.
        User user = createUser("ssar");
        Board board = createBoard("첫 번째 게시글", "첫 번째 내용", user);
        em.flush();
        Long boardId = board.getId();
        em.clear();

        // when: ID로 게시글을 조회한다.
        Optional<Board> result = boardJpaRepository.findByIdJoinUser(boardId);

        // then: 조회 결과가 존재하는지 확인한다.
        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("게시글 ID로 조회하면 작성자 정보도 함께 로딩된다")
    void findByIdJoinUser_작성자_함께로딩_테스트() {
        // given: 작성자와 게시글을 저장한 뒤 영속성 컨텍스트를 비운다.
        User user = createUser("ssar");
        Board board = createBoard("첫 번째 게시글", "첫 번째 내용", user);
        em.flush();
        Long boardId = board.getId();
        em.clear();

        // when: JOIN FETCH 쿼리로 게시글을 조회한다.
        Board foundBoard = boardJpaRepository.findByIdJoinUser(boardId)
                .orElseThrow();

        // then: 작성자 연관관계가 로딩되어 있는지 확인한다.
        PersistenceUnitUtil util = em.getEntityManagerFactory()
                .getPersistenceUnitUtil();

        assertThat(util.isLoaded(foundBoard, "user")).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 게시글 ID로 조회하면 빈 Optional을 반환한다")
    void findByIdJoinUser_존재하지않는_게시글_테스트() {
        // given: 존재하지 않는 ID를 사용한다.
        Long unknownId = 999999L;

        // when: 존재하지 않는 ID로 게시글을 조회한다.
        Optional<Board> result = boardJpaRepository.findByIdJoinUser(unknownId);

        // then: 조회 결과가 비어 있는지 확인한다.
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("전체 게시글을 첫 페이지에서 조회한다")
    void findAllJoinUser_첫페이지_조회_테스트() {
        // given: 게시글 3개를 저장한다.
        User user = createUser("ssar");
        createBoard("게시글1", "내용1", user);
        createBoard("게시글2", "내용2", user);
        createBoard("게시글3", "내용3", user);
        em.flush();
        em.clear();

        // when: 페이지 크기 2로 첫 페이지를 조회한다.
        Pageable pageable = PageRequest.of(0, 2);
        Page<Board> page = boardJpaRepository.findAllJoinUser(pageable);

        // then: 첫 페이지에 게시글 2개가 있는지 확인한다.
        assertThat(page.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("전체 게시글 조회 시 전체 개수가 반환된다")
    void findAllJoinUser_전체개수_테스트() {
        // given: 게시글 3개를 저장한다.
        User user = createUser("ssar");
        createBoard("게시글1", "내용1", user);
        createBoard("게시글2", "내용2", user);
        createBoard("게시글3", "내용3", user);
        em.flush();
        em.clear();

        // when: 페이지 크기 2로 전체 게시글을 조회한다.
        Page<Board> page = boardJpaRepository.findAllJoinUser(
                PageRequest.of(0, 2));

        // then: 전체 게시글 수가 3개인지 확인한다.
        assertThat(page.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("전체 게시글을 ID 내림차순으로 조회한다")
    void findAllJoinUser_ID내림차순_테스트() {
        // given: 게시글 3개를 순서대로 저장한다.
        User user = createUser("ssar");
        Board board1 = createBoard("게시글1", "내용1", user);
        Board board2 = createBoard("게시글2", "내용2", user);
        Board board3 = createBoard("게시글3", "내용3", user);
        em.flush();
        em.clear();

        // when: ID 내림차순 정렬 조건으로 조회한다.
        Pageable pageable = PageRequest.of(
                0, 10, Sort.by(Sort.Direction.DESC, "id"));
        Page<Board> page = boardJpaRepository.findAllJoinUser(pageable);

        // then: 게시글 ID가 내림차순으로 반환되는지 확인한다.
        assertThat(page.getContent())
                .extracting(Board::getId)
                .containsExactly(board3.getId(), board2.getId(), board1.getId());
    }

    @Test
    @DisplayName("전체 게시글 조회 시 작성자 정보가 함께 로딩된다")
    void findAllJoinUser_작성자_함께로딩_테스트() {
        // given: 작성자와 게시글을 저장한 뒤 영속성 컨텍스트를 비운다.
        User user = createUser("ssar");
        createBoard("게시글1", "내용1", user);
        createBoard("게시글2", "내용2", user);
        em.flush();
        em.clear();

        // when: 게시글 페이지를 조회한다.
        Page<Board> page = boardJpaRepository.findAllJoinUser(
                PageRequest.of(0, 10));

        // then: 조회한 모든 게시글의 user 연관관계가 로딩되었는지 확인한다.
        PersistenceUnitUtil util = em.getEntityManagerFactory()
                .getPersistenceUnitUtil();

        assertThat(page.getContent()).allSatisfy(board ->
                assertThat(util.isLoaded(board, "user")).isTrue());
    }

    @Test
    @DisplayName("제목에 포함된 키워드로 게시글을 검색한다")
    void findAllKeywordJoinUser_제목일치_테스트() {
        // given: 제목에 검색어가 있는 게시글과 없는 게시글을 저장한다.
        User user = createUser("ssar");
        createBoard("스프링 부트 테스트", "기초 내용", user);
        createBoard("자바 기초", "일반 내용", user);
        em.flush();
        em.clear();

        // when: 제목에 포함된 검색어로 게시글을 검색한다.
        Page<Board> page = boardJpaRepository.findAllKeywordJoinUser(
                PageRequest.of(0, 10), "스프링");

        // then: 검색 결과에 일치하는 제목만 포함되는지 확인한다.
        assertThat(page.getContent())
                .extracting(Board::getTitle)
                .containsExactly("스프링 부트 테스트");
    }

    @Test
    @DisplayName("내용에 포함된 키워드로 게시글을 검색한다")
    void findAllKeywordJoinUser_내용일치_테스트() {
        // given: 내용에 검색어가 있는 게시글과 없는 게시글을 저장한다.
        User user = createUser("ssar");
        createBoard("게시글1", "JPA 검색 테스트", user);
        createBoard("게시글2", "일반 내용", user);
        em.flush();
        em.clear();

        // when: 내용에 포함된 검색어로 게시글을 검색한다.
        Page<Board> page = boardJpaRepository.findAllKeywordJoinUser(
                PageRequest.of(0, 10), "JPA");

        // then: 내용이 일치하는 게시글만 조회되는지 확인한다.
        assertThat(page.getContent())
                .extracting(Board::getTitle)
                .containsExactly("게시글1");
    }

    @Test
    @DisplayName("제목과 내용이 모두 키워드와 일치하지 않으면 결과가 없다")
    void findAllKeywordJoinUser_불일치_테스트() {
        // given: 검색어가 제목과 내용에 없는 게시글을 저장한다.
        User user = createUser("ssar");
        createBoard("자바 기초", "객체지향 학습", user);
        em.flush();
        em.clear();

        // when: 존재하지 않는 키워드로 검색한다.
        Page<Board> page = boardJpaRepository.findAllKeywordJoinUser(
                PageRequest.of(0, 10), "없는키워드");

        // then: 검색 결과가 비어 있는지 확인한다.
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    @DisplayName("빈 키워드로 검색하면 제목과 내용이 있는 게시글이 조회된다")
    void findAllKeywordJoinUser_빈키워드_테스트() {
        // given: 제목과 내용이 모두 존재하는 게시글 2개를 저장한다.
        User user = createUser("ssar");
        createBoard("게시글1", "내용1", user);
        createBoard("게시글2", "내용2", user);
        em.flush();
        em.clear();

        // when: 빈 문자열로 검색한다.
        Page<Board> page = boardJpaRepository.findAllKeywordJoinUser(
                PageRequest.of(0, 10), "");

        // then: 두 게시글 모두 검색되는지 확인한다.
        assertThat(page.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("검색 결과의 전체 개수와 페이지 수를 확인한다")
    void findAllKeywordJoinUser_페이지정보_테스트() {
        // given: 동일한 키워드를 포함하는 게시글 3개를 저장한다.
        User user = createUser("ssar");
        createBoard("스프링1", "테스트 내용", user);
        createBoard("스프링2", "테스트 내용", user);
        createBoard("스프링3", "테스트 내용", user);
        em.flush();
        em.clear();

        // when: 페이지 크기 2로 검색한다.
        Page<Board> page = boardJpaRepository.findAllKeywordJoinUser(
                PageRequest.of(0, 2), "스프링");

        // then: 검색 결과의 전체 페이지 수가 2인지 확인한다.
        assertThat(page.getTotalPages()).isEqualTo(2);
    }
}
