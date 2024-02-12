package com.example.crawler.domain.lecture;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
@Service
public class LectureService {

    private final LectureRepository lectureRepository;
    private final EntityManager entityManager;

    @Transactional
    public void saveOrUpdateLectures(String source, List<Lecture> lectures) {

        List<Lecture> savedLectures = lectureRepository.findBySource(source);

        // Delete all lectures
        savedLectures.forEach(Lecture::delete);

        Map<String, Lecture> savedLectureMap = savedLectures.stream()
                .collect(Collectors.toMap(Lecture::getSourceId, Function.identity()));

        List<Lecture> newLectures = new ArrayList<>();

        // Update existing lectures
        for (Lecture lecture : lectures) {

            Lecture savedLecture = savedLectureMap.get(lecture.getSourceId());

            if (savedLecture == null) {
                newLectures.add(lecture);
            } else {
                // Update
                savedLecture.update(lecture);
            }
        }

        lectureRepository.saveAll(newLectures);
    }
}
