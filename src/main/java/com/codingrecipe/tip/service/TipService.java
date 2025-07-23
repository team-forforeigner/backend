package com.codingrecipe.tip.service;

import com.codingrecipe.tip.TipCategory;
import com.codingrecipe.tip.TipDTO;
import com.codingrecipe.tip.TipEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TipService {

    Page<TipEntity> getTips(Pageable pageable);
    Page<TipEntity> getTipsByCategory(TipCategory category, Pageable pageable);
    TipEntity saveTip(TipEntity tip);
    Page<TipDTO> findByCategory(TipCategory category, Pageable pageable);

    Page<TipDTO> findAll(Pageable pageable);
}
