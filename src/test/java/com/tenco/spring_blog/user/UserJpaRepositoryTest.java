
package com.tenco.spring_blog.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserJpaRepositoryTest {

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private EntityManager entityManager;

    // 테스트용 사용자 생성: User의 빌더에 username, password, email이 있다고 가정
    private User 사용자_생성(String username, String email) {
        User user = User.builder()
                .username(username)
                .password("encoded-password")
                .email(email)
                .build();

        return userJpaRepository.save(user);
    }

    @Test
    @DisplayName("사용자명으로 조회하면 사용자가 반환된다")
    void findByUsernameWithRoles_존재하는_사용자_테스트() {
        // given: 테스트에서 사용할 사용자를 직접 저장한다.
        사용자_생성("test-user", "test@example.com");

        // when: 사용자명으로 권한을 함께 조회한다.
        Optional<User> result =
                userJpaRepository.findByUsernameWithRoles("test-user");

        // then: 조회 결과가 존재하고 사용자명이 일치하는지 확인한다.
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("test-user");
    }

    @Test
    @DisplayName("존재하지 않는 사용자명으로 조회하면 빈 Optional을 반환한다")
    void findByUsernameWithRoles_존재하지않는_사용자_테스트() {
        // given: 조회할 사용자명을 가진 데이터를 저장하지 않는다.

        // when: 존재하지 않는 사용자명으로 조회한다.
        Optional<User> result =
                userJpaRepository.findByUsernameWithRoles("unknown-user");

        // then: 조회 결과가 비어 있는지 확인한다.
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("사용자 조회 시 roles 연관관계가 함께 로딩된다")
    void findByUsernameWithRoles_권한_함께로딩_테스트() {
        // given: 사용자를 저장한다.
        // 확인 필요: Role 생성 및 User-Role 연관관계 설정은 실제 엔티티 구조에 맞게 추가한다.
        사용자_생성("role-user", "role@example.com");

        // when: 영속성 컨텍스트를 비운 다음 조회하여 실제 DB 조회를 유도한다.
        entityManager.flush();
        entityManager.clear();

        User result = userJpaRepository
                .findByUsernameWithRoles("role-user")
                .orElseThrow();

        // then: JPA가 roles 연관관계를 로딩했는지 확인한다.
        PersistenceUnitUtil persistenceUnitUtil =
                entityManager.getEntityManagerFactory()
                        .getPersistenceUnitUtil();

        assertThat(persistenceUnitUtil.isLoaded(result, "roles"))
                .isTrue();
    }

    @Test
    @DisplayName("존재하는 사용자명으로 조회하면 해당 사용자가 반환된다")
    void findByUsername_존재하는_사용자_테스트() {
        // given: 사용자명을 가진 사용자를 저장한다.
        사용자_생성("green", "green@example.com");

        // when: 사용자명으로 조회한다.
        Optional<User> result = userJpaRepository.findByUsername("green");

        // then: 해당 사용자가 조회되는지 확인한다.
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("green");
    }

    @Test
    @DisplayName("존재하지 않는 사용자명으로 조회하면 빈 Optional을 반환한다")
    void findByUsername_존재하지않는_사용자_테스트() {
        // given: 해당 사용자명을 가진 사용자를 저장하지 않는다.

        // when: 존재하지 않는 사용자명으로 조회한다.
        Optional<User> result =
                userJpaRepository.findByUsername("not-exists");

        // then: 조회 결과가 비어 있는지 확인한다.
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("존재하는 이메일로 조회하면 해당 사용자가 반환된다")
    void findByEmail_존재하는_이메일_테스트() {
        // given: 이메일을 가진 사용자를 저장한다.
        사용자_생성("email-user", "green@example.com");

        // when: 이메일로 조회한다.
        Optional<User> result =
                userJpaRepository.findByEmail("green@example.com");

        // then: 해당 사용자의 이메일이 일치하는지 확인한다.
        assertThat(result).isPresent();
        assertThat(result.get().getEmail())
                .isEqualTo("green@example.com");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 조회하면 빈 Optional을 반환한다")
    void findByEmail_존재하지않는_이메일_테스트() {
        // given: 해당 이메일을 가진 사용자를 저장하지 않는다.

        // when: 존재하지 않는 이메일로 조회한다.
        Optional<User> result =
                userJpaRepository.findByEmail("unknown@example.com");

        // then: 조회 결과가 비어 있는지 확인한다.
        assertThat(result).isEmpty();
    }
}
