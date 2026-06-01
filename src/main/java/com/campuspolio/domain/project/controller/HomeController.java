package com.campuspolio.domain.project.controller;

import com.campuspolio.domain.project.dto.response.HomeResponse;
import com.campuspolio.domain.project.service.HomeService;
import com.campuspolio.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/home")
public class HomeController {

    private final HomeService homeService;

    @GetMapping
    public ApiResponse<HomeResponse> getHome() {

        return ApiResponse.success(
                homeService.getHome()
        );
    }
}