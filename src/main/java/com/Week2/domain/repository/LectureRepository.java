package com.Week2.domain.repository;

import com.Week2.domain.model.Lecture;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import jakarta.persistence.LockModeType;
@Repository
public class LectureRepository {

    @PersistenceContext
    private EntityManager entityManager;

    // 강의 아이디로 조회
    public Optional<Lecture> findById(Long id) {
        Lecture lecture = entityManager.find(Lecture.class, id);
        if (lecture != null && "ACTIVE".equals(lecture.getStatus())) {
            return Optional.of(lecture);
        }
        return Optional.empty();  // ACTIVE 상태가 아닌 경우 빈 Optional 반환
    }

    // 모든 강의 조회
    public List<Lecture> findAll() {
        return entityManager.createQuery("SELECT l FROM Lecture l WHERE l.status = 'ACTIVE'", Lecture.class)
                .getResultList();
    }

    // 강의 저장
    @Transactional
    public void save(Lecture lecture) {
        entityManager.persist(lecture);
    }

    // 강의 업데이트
    @Transactional
    public void update(Lecture lecture) {
        entityManager.merge(lecture);
    }

    // 강의 삭제
//    @Transactional
//    public void deleteById(Long id) {
//        Lecture lecture = entityManager.find(Lecture.class, id);
//        if (lecture != null) {
//            entityManager.remove(lecture);
//        }
//    }

    @Transactional
    public void deactivateLecture(Long id) {
        Lecture lecture = entityManager.find(Lecture.class, id);
        if (lecture != null && "ACTIVE".equals(lecture.getStatus())) { // 상태가 ACTIVE일 때만 비활성화
            lecture.setStatus("INACTIVE");
            entityManager.merge(lecture); // 업데이트 반영
        }
    }

    @Transactional
    public Optional<Lecture> findByIdWithLock(Long id) {
        try {
            String jpql = "SELECT l FROM Lecture l WHERE l.idx = :id";
            Lecture lecture = entityManager.createQuery(jpql, Lecture.class)
                    .setParameter("id", id)
                    .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                    .getSingleResult();
            return Optional.of(lecture);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

}
