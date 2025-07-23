package com.codingrecipe.tip.service;

import com.codingrecipe.tip.TipDTO;
import com.codingrecipe.tip.TipEntity;
import com.codingrecipe.tip.repository.TipRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/*
* Serivce 설명 : JSON 파일로 여러 Tip을 한 번에 DB에 저장하는 서비스입니다.
* 흐름 : TipDTO > TipEntity > TipRepository를 통해 DB에 저장
*/

@Service
@RequiredArgsConstructor
public class TipImportService {

    private final TipRepository tipRepository;
    private final ObjectMapper objectMapper;

    // 설명 : JSON 파일로부터 DTO -> Entity 변환 후 DB에 저장
    public void importFromJson(List<TipDTO> tipList) {
        List<TipEntity> entities = tipList.stream()
                .map(dto -> TipEntity.builder()
                        .question(dto.getQuestion())
                        .answer(dto.getAnswer())
                        .source(dto.getSource())
                        .categories(dto.getCategories()) // 복수 카테고리
                        .build())
                .collect(Collectors.toList());

        tipRepository.saveAll(entities);
    }


}
