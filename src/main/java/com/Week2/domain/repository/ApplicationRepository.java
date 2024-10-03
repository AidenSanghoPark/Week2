package com.Week2.domain.repository;

import com.Week2.domain.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public class ApplicationRepository {

    @PersistenceContext
    private EntityManager entityManager;

    // Application 엔티티 관련 메서드
    public Optional<Application> findApplicationById(Long id) {
        return Optional.ofNullable(entityManager.find(Application.class, id));
    }

    public List<Application> findAllApplications() {
        return entityManager.createQuery("SELECT a FROM Application a", Application.class).getResultList();
    }

    @Transactional
    public void save(Application application) {

        //  entityManager.persist(application);
        entityManager.merge(application); // merge 사용
    }

    // 추가: 중복 신청 확인 메서드
    public boolean existsByLecture_IdAndUser_Id(Long lectureId, Long userId) {
        String jpql = "SELECT COUNT(a) FROM Application a WHERE a.lecture_Id = :lectureId AND a.user_Id = :userId";
        Long count = entityManager.createQuery(jpql, Long.class)
                .setParameter("lectureId", lectureId)
                .setParameter("userId", userId)
                .getSingleResult();
        return count > 0;
    }
}
