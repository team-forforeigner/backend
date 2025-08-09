// 엔티티들이 공통으로 사용할 생성/수정 시간 필드를 제공하는 상위 클래스
package com.codingrecipe.board.domain;

import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public class BaseEntity {
    @CreationTimestamp // 엔티티가 처음 생성될 때 시간 자동 저장
    @Column(updatable = false) // 수정 시에는 이 필드가 업데이트되지 않도록 설정
    private LocalDateTime createdTime;

    @UpdateTimestamp // 엔티티가 수정될 때마다 시간 자동 저장
    @Column(insertable = false) // 생성 시에는 이 필드가 null로 저장되도록 설정
    private LocalDateTime updatedTime;
}
