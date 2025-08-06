package com.codingrecipe.tip.service;

import com.codingrecipe.tip.domain.TipCategory;
import com.codingrecipe.tip.dto.*;
import com.codingrecipe.tip.entity.TipEntity;
import com.codingrecipe.tip.exception.TipAlreadyExistsException;
import com.codingrecipe.tip.exception.TipNotFoundException;
import com.codingrecipe.tip.repository.TipRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
* 설명 : TipService 인터페이스를 구현한 서비스입니다.
*/

@Service
@RequiredArgsConstructor
public class TipServiceImpl implements TipService {

    private final TipRepository tipRepository;

    // 설명 : 팁 1개 저장
    @Override
    @Transactional
    public Long createTip(@NotNull TipCreateRequest dto) {
        String normalizedQuestion = dto.getQuestion().replaceAll("\\s+", ""); // 모든 공백 제거

        boolean exists = tipRepository.existsByQuestionIgnoreSpace(normalizedQuestion);
        if (exists) {
            throw new TipAlreadyExistsException("이미 등록된 질문입니다.");
        }

        String source = StringUtils.hasText(dto.getSource()) ? dto.getSource() : null;
        dto.setSource(source);

        TipEntity entity = dto.toEntity();
        TipEntity saved = tipRepository.save(entity);
        return saved.getId();
    }

    // 설명 : 팁 리스트 조회
    @Override
    public Page<TipResponse> getTips(Pageable pageable) {
        return tipRepository.findAll(pageable)
                .map(TipResponse::fromEntity);
    }

    // 설명 : 팁 1개 조회
    @Override
    public TipResponse getTipById(Long id) {
        TipEntity tip = tipRepository.findById(id)
                .orElseThrow(() -> new TipNotFoundException(id));
        return TipResponse.fromEntity(tip);
    }

    // 설명 : 카테고리별 팁 조회
    @Override
    public Page<TipResponse> getTipsByCategory(TipCategory category, Pageable pageable) {
        return tipRepository.findByCategory(category, pageable)
                .map(TipResponse::fromEntity);
    }

    // 설명 : 팁 업데이트
    @Override
    @Transactional
    public boolean updateTip(Long id, @NotNull TipUpdateRequest dto) {
        TipEntity tip = tipRepository.findById(id)
                .orElseThrow(() -> new TipNotFoundException(id));


        boolean changed = false;

        // question: 중복 검사 포함
        if (!Objects.equals(tip.getQuestion(), dto.getQuestion())) {
            if (tipRepository.existsByQuestion(dto.getQuestion())) {
                throw new TipAlreadyExistsException("이미 등록된 질문입니다.");
            }
            tip.setQuestion(dto.getQuestion());
            changed = true;
        }

        // 나머지 필드: 단순 비교 후 변경
        if (!Objects.equals(tip.getAnswer(), dto.getAnswer())) { tip.setAnswer(dto.getAnswer()); changed = true; }
        if (!Objects.equals(tip.getSource(), dto.getSource())) { tip.setSource(dto.getSource()); changed = true; }
        if (!Objects.equals(tip.getCategory(), dto.getCategory())) { tip.setCategory(dto.getCategory()); changed = true; }

        if (changed) tipRepository.save(tip);

        return changed; // 변경 여부 반환
    }

    // 설명 : 팁 삭제
    @Override
    @Transactional
    public void deleteTip(Long id) {
        TipEntity entity = tipRepository.findById(id)
                .orElseThrow(() -> new TipNotFoundException(id));
        tipRepository.delete(entity);
    }

    // 설명 : JSON 배열로 받아서 DB 저장
    @Transactional
    public String importFromJson(List<TipCreateRequest> tipList) {
        // 설명 : 정상적인 팁들은 저장하고, 중복된 팁들은 수집해서 한 번에 보고 한다.
        List<String> duplicatedQuestions = new ArrayList<>();
        List<TipEntity> validTips = new ArrayList<>();
        String result;

        for (TipCreateRequest dto : tipList) {
            if (isDuplicated(dto.getQuestion())) {
                duplicatedQuestions.add(dto.getQuestion());
            } else {
                String source = StringUtils.hasText(dto.getSource()) ? dto.getSource() : null;
                dto.setSource(source);
                validTips.add(dto.toEntity());
            }
        }
        // 정상적인 팁들 저장
        if (!validTips.isEmpty()) {
            tipRepository.saveAll(validTips);
        }
        result = validTips.size() + "개의 팁이 저장되었습니다. 중복된 질문: " + duplicatedQuestions;

        return result;
    }

    // 설명 : 질문 중복 확인
    private boolean isDuplicated(String question) {
        return tipRepository.existsByQuestion(question);
    }


}
