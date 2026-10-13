package com.tenco.spring_blog.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserJpaRepositoryTest {

    @Autowired
    private UserJpaRepository userJpaRepository;

    // Given-When-Then: 사용자명과 비밀번호로 사용자 조회
    @Test
    @DisplayName("사용자명과 비밀번호로 로그인 사용자를 조회할 수 있다")
    void findByUsernameAndPassword_test() {
        // given
        User user = User.builder()
                .username("ssar")
                .password("1234")
                .email("ssar@test.com")
                .build();

        userJpaRepository.save(user);

        // when
        Optional<User> result =
                userJpaRepository.findByUsernameAndPassword("ssar", "1234");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("ssar");
        assertThat(result.get().getPassword()).isEqualTo("1234");
    }

    @Test
    @DisplayName("잘못된 비밀번호로 조회하면 사용자가 존재하지 않는다")
    void findByUsernameAndPassword_fail_test() {
        // given
        User user = User.builder()
                .username("ssar")
                .password("1234")
                .email("ssar@test.com")
                .build();

        userJpaRepository.save(user);

        // when
        Optional<User> result =
                userJpaRepository.findByUsernameAndPassword("ssar", "wrong");

        // then
        assertThat(result).isEmpty();
    }

    // Given-When-Then: 사용자명으로 사용자 조회
    @Test
    @DisplayName("사용자명으로 사용자를 조회할 수 있다")
    void findByUsername_test() {
        // given
        User user = User.builder()
                .username("cos")
                .password("5678")
                .email("cos@test.com")
                .build();

        userJpaRepository.save(user);

        // when
        Optional<User> result = userJpaRepository.findByUsername("cos");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("cos");
    }

    @Test
    @DisplayName("존재하지 않는 사용자명으로 조회하면 빈 Optional을 반환한다")
    void findByUsername_fail_test() {
        // given
        String username = "unknown";

        // when
        Optional<User> result = userJpaRepository.findByUsername(username);

        // then
        assertThat(result).isEmpty();
    }

    // Given-When-Then: 이메일로 사용자 조회
    @Test
    @DisplayName("이메일로 사용자를 조회할 수 있다")
    void findByEmail_test() {
        // given
        User user = User.builder()
                .username("love")
                .password("1234")
                .email("love@test.com")
                .build();

        userJpaRepository.save(user);

        // when
        Optional<User> result = userJpaRepository.findByEmail("love@test.com");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("love@test.com");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 조회하면 빈 Optional을 반환한다")
    void findByEmail_fail_test() {
        // given
        String email = "unknown@test.com";

        // when
        Optional<User> result = userJpaRepository.findByEmail(email);

        // then
        assertThat(result).isEmpty();
    }

    // Given-When-Then: 사용자 저장
    @Test
    @DisplayName("사용자를 저장할 수 있다")
    void save_test() {
        // given
        User user = User.builder()
                .username("admin")
                .password("1234")
                .email("admin@test.com")
                .build();

        // when
        User savedUser = userJpaRepository.save(user);

        // then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("admin");
        assertThat(savedUser.getEmail()).isEqualTo("admin@test.com");
    }
}