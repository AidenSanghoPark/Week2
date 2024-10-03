package com.Week2.intergration;

import com.Week2.Week2Application;
import com.Week2.application.service.ApplicationService;
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

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = Week2Application.class)
@ActiveProfiles("test")
public class LectureDuplicateApplicationIntegrationTest {

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
        Lecture lecture = new Lecture();
        lecture.setTitle("Test Lecture");
        lecture.setMax_Participants(30);
        lecture.setCurrent_Participants(0);
        lecture.setStatus("ACTIVE");
        lectureRepository.save(lecture);
        lectureId = lecture.getIdx();
    }

    @Test
    @Transactional
    public void testMultipleApplicationsSameUser() {
        Long userId = 1L;
        AtomicInteger successfulApplications = new AtomicInteger(0);
        AtomicInteger failedApplications = new AtomicInteger(0);

        IntStream.range(0, 5).forEach(i -> {
            try {
                applicationService.applyForLecture(lectureId, userId);
                successfulApplications.incrementAndGet();
            } catch (Exception e) {
                failedApplications.incrementAndGet();
            }
        });

        assertEquals(1, successfulApplications.get(), "Only one application should succeed");
        assertEquals(4, failedApplications.get(), "Four applications should fail");

        List<Application> applications = applicationRepository.findAllApplications();
        assertEquals(1, applications.size(), "Only one application should be in the database");

        Lecture updatedLecture = lectureRepository.findById(lectureId).orElseThrow();
        assertEquals(1, updatedLecture.getCurrent_Participants(), "Lecture should have 1 participant");
    }

    @Test
    @Transactional
    public void testConcurrentApplicationsSameUser() throws InterruptedException {
        int numberOfThreads = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger successfulApplications = new AtomicInteger(0);
        AtomicInteger failedApplications = new AtomicInteger(0);

        Long userId = 1L;

        for (int i = 0; i < numberOfThreads; i++) {
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

        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        assertEquals(1, successfulApplications.get(), "한명만 성공되어야 함");
        assertEquals(4, failedApplications.get(), "네명은 실패되어야 함");

        List<Application> applications = applicationRepository.findAllApplications();
        assertEquals(1, applications.size(), "한명만 DB에 있어야 함 ");

        Lecture updatedLecture = lectureRepository.findById(lectureId).orElseThrow();
        assertEquals(1, updatedLecture.getCurrent_Participants(), "1명의 참가자만 업데이트 되어있어야 함");
    }
}