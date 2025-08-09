// 게시판의 카테고리(자유게시판, 정보게시판, 뉴비게시판) 정보를 관리하는 엔티티
package com.codingrecipe.board.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "category_table") // 'category_table' 테이블과 매핑
public class CategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 카테고리 고유 식별자

    @Column(length = 50, unique = true, nullable = false) // 길이 50, 유니크, null 불가 제약조건
    private String name; // "자유게시판", "정보게시판" 등 카테고리 이름
}
