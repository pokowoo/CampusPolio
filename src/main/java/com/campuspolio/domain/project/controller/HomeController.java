package com.campuspolio.domain.project.controller;

import com.campuspolio.domain.project.dto.response.HomeResponse;
import com.campuspolio.domain.project.service.HomeService;
import com.campuspolio.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Home",
        description = "메인 페이지 API"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/home")
public class HomeController {

    private final HomeService homeService;

    @Operation(
            summary = "메인 페이지 조회",
            description = "메인 페이지에 표시할 주요 프로젝트, 인기 프로젝트, 최신 프로젝트 등의 정보를 조회합니다."
    )
    @GetMapping
    public ApiResponse<HomeResponse> getHome() {

        return ApiResponse.success(
                homeService.getHome()
        );
    }
}