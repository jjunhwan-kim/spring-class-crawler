package com.example.crawler.domain;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Getter
@RequiredArgsConstructor
@Service
public class LectureService {

    private final LectureRepository lectureRepository;
    private final EntityManager entityManager;

    @Transactional
    public void save(List<Lecture> lectures) {
        lectureRepository.deleteAllInBatch();
        entityManager.flush();
        entityManager.clear();
        lectureRepository.saveAll(lectures);
    }
}
