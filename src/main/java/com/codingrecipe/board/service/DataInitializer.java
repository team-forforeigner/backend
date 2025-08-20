package com.codingrecipe.board.service;

import com.codingrecipe.board.domain.CategoryEntity;
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
        initCategory("자유게시판");
        initCategory("정보게시판");
        initCategory("뉴비게시판");
        initCategory("공지사항"); // 관리자 전용 '공지사항' 카테고리
    }

    private void initCategory(String name) {
        if (categoryRepository.findByName(name).isEmpty()) {
            CategoryEntity category = new CategoryEntity();
            category.setName(name);
            categoryRepository.save(category);
        }
    }
}
