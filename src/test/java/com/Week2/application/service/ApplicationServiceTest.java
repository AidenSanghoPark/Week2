package com.Week2.application.service;

import com.Week2.domain.model.Application;
import com.Week2.domain.model.Lecture;
import com.Week2.domain.repository.ApplicationRepository;
import com.Week2.domain.repository.LectureRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private LectureRepository lectureRepository;

    @InjectMocks
    private ApplicationService applicationService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    // 10명 정원에 5명 참가자 있을때 강의 신청 테스트
    @Test
    void applyForLecture_SuccessfulApplication() {
        Long lectureId = 1L;
        Long userId = 1L;
        Lecture lecture = new Lecture();
        lecture.setIdx(lectureId);
        lecture.setMax_Participants(10);
        lecture.setCurrent_Participants(5);

        when(lectureRepository.findById(lectureId)).thenReturn(Optional.of(lecture));

        assertDoesNotThrow(() -> applicationService.applyForLecture(lectureId, userId));

        verify(applicationRepository, times(1)).save(any(Application.class));
        verify(lectureRepository, times(1)).update(any(Lecture.class));
    }

    // 강의가 가득찬 경우
    @Test
    void applyForLecture_LectureFull() {
        Long lectureId = 1L;
        Long userId = 1L;
        Lecture lecture = new Lecture();
        lecture.setIdx(lectureId);
        lecture.setMax_Participants(10);
        lecture.setCurrent_Participants(10);

        when(lectureRepository.findById(lectureId)).thenReturn(Optional.of(lecture));

        assertThrows(IllegalArgumentException.class, () -> applicationService.applyForLecture(lectureId, userId));

        verify(applicationRepository, never()).save(any(Application.class));
        verify(lectureRepository, never()).update(any(Lecture.class));
    }
}