package com.codingrecipe.tip.service;

import com.codingrecipe.tip.TipCategory;
import com.codingrecipe.tip.dto.TipDTO;
import com.codingrecipe.tip.entity.TipEntity;
import com.codingrecipe.tip.exception.TipAlreadyExistsException;
import com.codingrecipe.tip.exception.TipNotFoundException;
import com.codingrecipe.tip.repository.TipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
* 설명 : TipService 인터페이스를 구현한 서비스입니다.
*/

@Service
@RequiredArgsConstructor
public class TipServiceImpl implements TipService {

    private final TipRepository tipRepository;

    // 설명 : 팁 조회
    @Override
    public Page<TipDTO> getTips(Pageable pageable) {
        return tipRepository.findAll(pageable)
                .map(TipDTO::fromEntity);
    }

    // 설명 : 카테고리별 팁 조회
    @Override
    public Page<TipDTO> getTipsByCategory(TipCategory category, Pageable pageable) {
        return tipRepository.findByCategory(category, pageable)
                .map(TipDTO::fromEntity);
    }

    // 설명 : 팁 업데이트
    @Override
    @Transactional
    public void updateTip(Long id, TipDTO dto) {
        TipEntity tip = tipRepository.findById(id)
                .orElseThrow(() -> new TipNotFoundException(id));

        tip.setQuestion(dto.getQuestion());
        tip.setAnswer(dto.getAnswer());
        tip.setSource(dto.getSource());
        tip.setCategory(dto.getCategory());

        tipRepository.save(tip);
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
    public void importFromJson(List<TipDTO> tipList) {
        // 설명 : 정상적인 팁은 저장하고, 중복된 팁은 수집해서 한 번에 보고 한다.
        List<String> duplicatedQuestions = new ArrayList<>();
        List<TipEntity> validTips = new ArrayList<>();

        for (TipDTO dto : tipList) {
            if (tipRepository.existsByQuestion(dto.getQuestion())) {
                duplicatedQuestions.add(dto.getQuestion());
            } else {
                if (isValid(dto)) {
                    validTips.add(dto.toEntity());
                } else {
                    // 유효성 검사 실패 처리도 추가 가능
                    throw new IllegalArgumentException("유효하지 않은 데이터: " + dto.getQuestion());
                }
            }
        }

        if (!duplicatedQuestions.isEmpty()) {
            throw new TipAlreadyExistsException("중복된 질문들: " + duplicatedQuestions);
        }

        tipRepository.saveAll(validTips);
    }

    // 설명 : DTO 유효성 검사
    private boolean isValid(TipDTO dto) {
        return StringUtils.hasText(dto.getQuestion()) &&
                StringUtils.hasText(dto.getAnswer()) &&
                dto.getCategory() != null;
    }


}
