package com.codingrecipe.tip.service;

import com.codingrecipe.tip.dto.TipDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TipService {

    Page<TipDTO> getTips(Pageable pageable);
    void importFromJson(List<TipDTO> tipList);

}
