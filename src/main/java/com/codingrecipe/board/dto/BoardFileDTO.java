package com.codingrecipe.board.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class BoardFileDTO {
    private Long id;
    private String originalFileName;
    private String storedFileName;
    private Long boardId;
}