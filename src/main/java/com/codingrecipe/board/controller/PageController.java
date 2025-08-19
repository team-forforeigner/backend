package com.codingrecipe.board.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * S3 '로컬 테스트'를 위한 페이지 컨트롤러
 */

@Controller
@RequiredArgsConstructor
public class PageController {

    // http://localhost:8080/api/s3/page
    @GetMapping("/api/s3/page")
    public String s3Page() {
        return "s3-crud";
    }

}
