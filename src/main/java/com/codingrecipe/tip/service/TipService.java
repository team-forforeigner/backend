package com.codingrecipe.tip.service;

import com.codingrecipe.tip.TipCategory;
import com.codingrecipe.tip.dto.TipDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TipService {

    // 설명 : 팁 리스트 조회 (Page 적용)
    Page<TipDTO> getTips(Pageable pageable);

    // 설명 : 카테고리별 팁 조회 (Page 적용)
    Page<TipDTO> getTipsByCategory(TipCategory category, Pageable pageable);

    // 설명 : 팁 업데이트
    void updateTip(Long id, TipDTO dto);

    // 설명 : 팁 삭제
    void deleteTip(Long id);

    // 설명 : JSON 배열로 받아서 DB 저장
    void importFromJson(List<TipDTO> tipList);

}
