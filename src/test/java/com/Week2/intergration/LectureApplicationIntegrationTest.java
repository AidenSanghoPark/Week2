package com.Week2.intergration;

import com.Week2.Week2Application;
import com.Week2.application.service.ApplicationService;
import com.Week2.application.service.LectureService;
import com.Week2.domain.model.Application;
import com.Week2.domain.model.Lecture;
import com.Week2.domain.repository.ApplicationRepository;
import com.Week2.domain.repository.LectureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = Week2Application.class)
@ActiveProfiles("test")
public class LectureApplicationIntegrationTest {

    @Autowired
    private LectureService lectureService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private LectureRepository lectureRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    private Long lectureId;

    @BeforeEach
    @Transactional
    public void setUp() {
        // 테스트용 강의 생성
        Lecture lecture = new Lecture();
        lecture.setTitle("Test Lecture");
        lecture.setMax_Participants(30);
        lecture.setCurrent_Participants(0);
        lecture.setStatus("ACTIVE");
        lectureRepository.save(lecture);
        lectureId = lecture.getIdx();
    }

    @Test
    public void testConcurrentLectureApplications() throws InterruptedException {
        int numberOfThreads = 40;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger successfulApplications = new AtomicInteger(0);
        AtomicInteger failedApplications = new AtomicInteger(0);

        // 각 유저아이디는 +1로 다르게구분
        for (int i = 0; i < numberOfThreads; i++) {
            long userId = i + 1;
            executorService.execute(() -> {
                try {
                    applicationService.applyForLecture(lectureId, userId);
                    successfulApplications.incrementAndGet();
                } catch (Exception e) {
                    failedApplications.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        // 스레드 종료시까지 기달
        latch.await();
        executorService.shutdown();

        assertEquals(30, successfulApplications.get(), "성공 인원은 30");
        assertEquals(10, failedApplications.get(), "실패 인원은 10명");

        Lecture updatedLecture = lectureRepository.findById(lectureId).orElseThrow();
        assertEquals(30, updatedLecture.getCurrent_Participants(), "현재 인원 30명");
        assertEquals("INACTIVE", updatedLecture.getStatus(), "강의 상태는 INACTIVE 여야 함");

        // 디비확인
        List<Application> applications = applicationRepository.findAllApplications();
        assertEquals(30, applications.size(), "30명이 데이터베이스에 있음");
    }
}