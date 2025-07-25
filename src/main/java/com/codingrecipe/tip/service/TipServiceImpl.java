package com.codingrecipe.tip.service;

import com.codingrecipe.tip.dto.TipDTO;
import com.codingrecipe.tip.entity.TipEntity;
import com.codingrecipe.tip.exception.TipAlreadyExistsException;
import com.codingrecipe.tip.repository.TipRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
* 설명 : TipService 인터페이스를 구현한 서비스입니다.
*/

@Service
@RequiredArgsConstructor
public class TipServiceImpl implements TipService {

    private final TipRepository tipRepository;

    // 설명 : TipEntity를 조회하고, TipDTO로 변환하여 반환한다.
    @Override
    public Page<TipDTO> getTips(Pageable pageable) {
        return tipRepository.findAll(pageable)
                .map(TipDTO::fromEntity);
    }

    @Transactional
    public void importFromJson(List<TipDTO> tipList) {
        for (TipDTO dto : tipList) {
            boolean exists = tipRepository.existsByQuestion(dto.getQuestion());
            if (exists) {
                throw new TipAlreadyExistsException("중복된 질문이 존재합니다: " + dto.getQuestion());
            }

            List<TipEntity> entities = tipList.stream()
                    .map(TipDTO::toEntity)
                    .collect(Collectors.toList());
            tipRepository.saveAll(entities);
        }
    }


}
