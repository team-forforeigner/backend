package com.codingrecipe.board.service;

import com.codingrecipe.board.entity.CategoryEntity;
import com.codingrecipe.board.repository.CategoryRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final CategoryRepository categoryRepository;

    @PostConstruct
    @Transactional
    public void init() {
        if (categoryRepository.count() == 0) { // 카테고리 테이블이 비어있을 때만 실행
            CategoryEntity freeBoard = new CategoryEntity();
            freeBoard.setName("자유게시판");
            categoryRepository.save(freeBoard);

            CategoryEntity infoBoard = new CategoryEntity();
            infoBoard.setName("정보게시판");
            categoryRepository.save(infoBoard);

            CategoryEntity newbieBoard = new CategoryEntity();
            newbieBoard.setName("뉴비게시판");
            categoryRepository.save(newbieBoard);
        }
    }
}