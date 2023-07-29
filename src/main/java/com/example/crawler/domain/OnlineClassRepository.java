package com.example.crawler.domain;


import org.springframework.data.jpa.repository.JpaRepository;

public interface OnlineClassRepository extends JpaRepository<OnlineClass, Long> {

    OnlineClass findByTitleAndUrl(String title, String url);
}
