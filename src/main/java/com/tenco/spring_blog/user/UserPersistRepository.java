package com.tenco.spring_blog.user;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;


@RequiredArgsConstructor // DI
@Repository // IoC
public class UserPersistRepository {

    private final EntityManager em;

    // 회원 가입
    @Transactional
    public User save(User user) {
        // 비영속 상태에 User 객체를 영속성 컨텍스트에 저장
        em.persist(user);
        // 영속성 컨텍스트가 user 객체를 관리하기 시작
        // persist() 후 객체는 영속 상태가 되고 트랜잭션 커밋 시점이 INSERT  쿼리가 실행 됨
        // 자동 생성된 ID와 생성시간이 user 객체에 설정 됨.
        return user;
    }

    // 사용자명 중복 체크용 조회 메서드
    public User findByUsername(String username) {
        // JPQL 사용 (em.find() 는 PK 기반으로 조회 함) 우리가 필요한건 username 기반으로 조회 해야 함.
        String jpql = """
                SELECT u FROM User u WHERE u.username = :username
                """;
        try {
            return em.createQuery(jpql, User.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (Exception e) {
            // 사용자를 찾을 수 없는 경우 null 반환
            return null;
        }
    }
}
