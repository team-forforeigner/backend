// 배너 정보를 데이터베이스에 저장하기 위한 엔티티 클래스
package com.codingrecipe.board.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "banner_table") // 'banner_table'이라는 이름의 테이블과 매핑
@Getter
@Setter
public class BannerEntity {

    @Id // 기본 키 필드임을 명시
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본 키 값 자동 생성 전략 (데이터베이스에 위임)
    private Long id; // 배너 고유 식별자

    @Column(nullable = false, length = 100) // null 불가, 길이 100 제한
    private String title; // 배너 제목

    @Column(length = 500) // 길이 500 제한
    private String description; // 배너 설명

    @Column(nullable = false) // null 불가
    private String imageUrl; // 배너 이미지 URL

    @Column(length = 500) // 길이 500 제한
    private String linkUrl; // 배너 클릭 시 이동할 링크 URL

    @Column(nullable = false) // null 불가
    private boolean isExposed; // 배너 노출 여부 (true: 노출, false: 비노출)

    private int displayOrder; // 배너 표시 순서
}
