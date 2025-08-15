package com.codingrecipe.board.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class PageController {

    // http://localhost:8080/api/s3/page
    @GetMapping("/api/s3/page")
    public String s3Page() {
        return "s3-crud";
    }

}
