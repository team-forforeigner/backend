package com.codingrecipe.tip.service;

import com.codingrecipe.tip.TipCategory;
import com.codingrecipe.tip.dto.TipCreateRequest;
import com.codingrecipe.tip.dto.TipResponse;
import com.codingrecipe.tip.dto.TipUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TipService {

    // 설명 : 팁 저장 (단일)
    public Long createTip(TipCreateRequest dto);

    // 설명 : 팁 리스트 조회 (Page 적용)
    Page<TipResponse> getTips(Pageable pageable);

    // 설명 : 카테고리별 팁 조회 (Page 적용)
    Page<TipResponse> getTipsByCategory(TipCategory category, Pageable pageable);

    // 설명 : 팁 업데이트
    boolean updateTip(TipUpdateRequest dto);

    // 설명 : 팁 삭제
    void deleteTip(Long id);

    // 설명 : JSON 배열로 받아서 DB 저장
    String importFromJson(List<TipCreateRequest> tipList);

}
