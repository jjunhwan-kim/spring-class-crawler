package com.example.crawler.domain;

import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Getter
@RequiredArgsConstructor
@Service
public class OnlineClassService {

    private final OnlineClassRepository onlineClassRepository;

    @Transactional
    public void saveOrUpdate(OnlineClass onlineClass) {

        OnlineClass foundClass = onlineClassRepository.findByTitleAndUrl(onlineClass.getTitle(), onlineClass.getUrl());
        if (foundClass == null) {
            onlineClassRepository.save(onlineClass);
        } else {
            foundClass.update(
                    onlineClass.getSource(),
                    onlineClass.getImageUrl(),
                    onlineClass.getPrice(),
                    onlineClass.getInstructor(),
                    onlineClass.getCategory1(),
                    onlineClass.getCategory2(),
                    onlineClass.getCategory3(),
                    onlineClass.getCategory4(),
                    onlineClass.getCategory5());
        }
    }
}
