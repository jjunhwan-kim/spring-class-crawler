package com.example.crawler.domain.lecture;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LectureRepository extends JpaRepository<Lecture, Long> {
    List<Lecture> findBySourceAndSourceIdIn(String source, List<String> ids);
}
