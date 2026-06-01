package com.campuspolio;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
public class HelloController {

    @GetMapping("/")
    public String hello() {
        return "https 테스트입니당 Hello World";
    }

}