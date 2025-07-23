package com.codingrecipe.tip.service;

import com.codingrecipe.tip.TipCategory;
import com.codingrecipe.tip.TipDTO;
import com.codingrecipe.tip.TipEntity;
import com.codingrecipe.tip.repository.TipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class TipServiceImpl implements TipService {

    private final TipRepository tipRepository;

    @Override
    public Page<TipEntity> getTips(Pageable pageable) {
        return tipRepository.findAll(pageable);
    }

    @Override
    public Page<TipEntity> getTipsByCategory(TipCategory category, Pageable pageable) {
        // 다대다 관계에서 category가 포함된 팁 검색
        return tipRepository.findByCategoriesIn(category, pageable);
    }

    @Override
    public TipEntity saveTip(TipEntity tip) {
        return tipRepository.save(tip);
    }

    @Override
    public Page<TipDTO> findByCategory(TipCategory category, Pageable pageable) {
        return null;
    }

    @Override
    public Page<TipDTO> findAll(Pageable pageable) {
        return null;
    }
}
