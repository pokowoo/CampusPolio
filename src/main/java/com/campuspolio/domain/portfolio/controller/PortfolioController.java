package com.campuspolio.domain.portfolio.controller;

import com.campuspolio.domain.portfolio.dto.*;
import com.campuspolio.domain.portfolio.service.PortfolioService;
import com.campuspolio.global.response.ApiResponse;
import com.campuspolio.global.security.AuthenticatedUser;
import com.campuspolio.global.security.SessionConst;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Portfolio", description = "포트폴리오 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PortfolioController {

    private final PortfolioService portfolioService;

    // =========================
    // 포트폴리오 생성
    // =========================

    @PostMapping("/portfolios")
    @Operation(
            summary = "포트폴리오 생성",
            description = """
                    현재 로그인한 사용자의 포트폴리오를 생성합니다.
                    
                    정책:
                    - 로그인 필수
                    - 대학 인증(universityVerified)이 완료된 사용자만 생성 가능
                    - title은 필수
                    - slug는 title 기반으로 자동 생성
                    - slug 중복 시 -1, -2 형태로 자동 증가
                    - 삭제된 slug도 재사용하지 않음
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "포트폴리오 생성 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 또는 제목 누락")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요함")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "대학 인증이 필요함")
    public ApiResponse<PortfolioCreateResponse> createPortfolio(
            @AuthenticatedUser Long userId,
            @Valid @RequestBody PortfolioCreateRequest request
    ) {
        PortfolioCreateResponse response = portfolioService.createPortfolio(
                userId,
                request
        );

        return ApiResponse.success(response);
    }

    // =========================
    // 내 포트폴리오 목록 조회
    // =========================

    @GetMapping("/users/me/portfolios")
    @Operation(
            summary = "내 포트폴리오 목록 조회",
            description = """
                현재 로그인한 사용자의 포트폴리오 목록을 조회합니다.
                
                정책:
                - 로그인 필수
                - soft delete된 포트폴리오는 제외
                - 기본 페이지 크기는 6
                - isPublic 파라미터가 없으면 전체 조회
                - isPublic=true이면 공개 포트폴리오만 조회
                - isPublic=false이면 비공개 포트폴리오만 조회
                """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "내 포트폴리오 목록 조회 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요함")
    public ApiResponse<MyPortfolioListResponse> getMyPortfolios(
            @AuthenticatedUser Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(required = false) Boolean isPublic
    ) {
        MyPortfolioListResponse response = portfolioService.getMyPortfolios(
                userId,
                page,
                size,
                isPublic
        );

        return ApiResponse.success(response);
    }

    // =========================
    // slug 기반 포트폴리오 상세 조회
    // =========================

    @GetMapping("/portfolios/{slug}")
    @Operation(
            summary = "slug 기반 포트폴리오 조회",
            description = """
                    slug를 기준으로 포트폴리오를 조회합니다.
                    
                    정책:
                    - 공개 포트폴리오는 비로그인 사용자도 접근 가능
                    - 비공개 포트폴리오는 본인만 접근 가능
                    - 비공개 포트폴리오에 타인이 접근하면 404 반환
                    - 포트폴리오 상세에서는 공개 + 발행 상태의 프로젝트만 노출
                    - soft delete된 포트폴리오와 프로젝트는 조회 제외
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "포트폴리오 조회 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 포트폴리오 또는 접근할 수 없는 비공개 포트폴리오")
    public ApiResponse<PortfolioDetailResponse> getPortfolioBySlug(
            @PathVariable String slug,
            HttpSession session
    ) {
        /*
         * 공개 포트폴리오는 비로그인도 접근 가능해야 하므로
         * @AuthenticatedUser를 사용하지 않고 세션에서 직접 userId를 꺼낸다.
         */
        Long loginUserId = (Long) session.getAttribute(SessionConst.LOGIN_USER_ID);

        PortfolioDetailResponse response = portfolioService.getPortfolioBySlug(
                slug,
                loginUserId
        );

        return ApiResponse.success(response);
    }

    // =========================
    // 포트폴리오 정보 수정
    // =========================

    @PatchMapping("/portfolios/{portfolioId}")
    @Operation(
            summary = "포트폴리오 정보 수정",
            description = """
                    포트폴리오의 기본 정보를 수정합니다.
                    
                    정책:
                    - 로그인 필수
                    - 본인 포트폴리오만 수정 가능
                    - title, description, thumbnailUrl 수정 가능
                    - title을 수정해도 slug는 변경하지 않음
                    - null로 전달한 필드는 기존 값 유지
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "포트폴리오 수정 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 또는 제목 누락")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요함")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "포트폴리오 권한 없음")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "포트폴리오 없음")
    public ApiResponse<PortfolioUpdateResponse> updatePortfolio(
            @AuthenticatedUser Long userId,
            @PathVariable Long portfolioId,
            @Valid @RequestBody PortfolioUpdateRequest request
    ) {
        PortfolioUpdateResponse response = portfolioService.updatePortfolio(
                userId,
                portfolioId,
                request
        );

        return ApiResponse.success(response);
    }

    // =========================
    // 포트폴리오 공개 / 비공개 변경
    // =========================

    @PatchMapping("/portfolios/{portfolioId}/visibility")
    @Operation(
            summary = "포트폴리오 공개/비공개 변경",
            description = """
                    포트폴리오의 공개 여부를 변경합니다.
                    
                    정책:
                    - 로그인 필수
                    - 본인 포트폴리오만 변경 가능
                    - 공개 전환 시 공개 + 발행 프로젝트가 1개 이상 포함되어야 함
                    - 비공개 전환은 언제든 가능
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "포트폴리오 공개 상태 변경 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "공개 조건 불충족")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요함")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "포트폴리오 권한 없음")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "포트폴리오 없음")
    public ApiResponse<PortfolioVisibilityUpdateResponse> updateVisibility(
            @AuthenticatedUser Long userId,
            @PathVariable Long portfolioId,
            @Valid @RequestBody PortfolioVisibilityUpdateRequest request
    ) {
        PortfolioVisibilityUpdateResponse response = portfolioService.updateVisibility(
                userId,
                portfolioId,
                request
        );

        return ApiResponse.success(response);
    }

    // =========================
    // 포트폴리오 삭제
    // =========================

    @DeleteMapping("/portfolios/{portfolioId}")
    @Operation(
            summary = "포트폴리오 삭제",
            description = """
                    포트폴리오를 삭제합니다.
                    
                    정책:
                    - 로그인 필수
                    - 본인 포트폴리오만 삭제 가능
                    - 포트폴리오는 soft delete 처리
                    - 포함된 프로젝트 자체는 삭제하지 않음
                    - 포트폴리오-프로젝트 연결만 제거
                    - 삭제된 slug는 재사용하지 않음
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "포트폴리오 삭제 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요함")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "포트폴리오 권한 없음")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "포트폴리오 없음")
    public ApiResponse<PortfolioDeleteResponse> deletePortfolio(
            @AuthenticatedUser Long userId,
            @PathVariable Long portfolioId
    ) {
        PortfolioDeleteResponse response = portfolioService.deletePortfolio(
                userId,
                portfolioId
        );

        return ApiResponse.success(response);
    }

    // =========================
    // 포트폴리오 프로젝트 추가 / 제거
    // =========================

    @PatchMapping("/portfolios/{portfolioId}/projects")
    @Operation(
            summary = "포트폴리오 프로젝트 추가/제거",
            description = """
                    포트폴리오에 프로젝트를 추가하거나 제거합니다.
                    
                    정책:
                    - 로그인 필수
                    - 본인 포트폴리오만 수정 가능
                    - 본인이 OWNER 또는 MEMBER인 프로젝트만 추가 가능
                    - 중복 프로젝트 추가 불가
                    - 최대 50개까지 추가 가능
                    - 프로젝트 제거 시 포트폴리오와 프로젝트의 연결만 제거
                    - 프로젝트 자체는 삭제하지 않음
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "포트폴리오 프로젝트 수정 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청, 중복 프로젝트, 최대 개수 초과")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요함")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "포트폴리오 또는 프로젝트 권한 없음")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "포트폴리오 또는 프로젝트 없음")
    public ApiResponse<PortfolioProjectUpdateResponse> updatePortfolioProjects(
            @AuthenticatedUser Long userId,
            @PathVariable Long portfolioId,
            @RequestBody PortfolioProjectUpdateRequest request
    ) {
        PortfolioProjectUpdateResponse response = portfolioService.updatePortfolioProjects(
                userId,
                portfolioId,
                request
        );

        return ApiResponse.success(response);
    }

    // =========================
    // 포트폴리오 프로젝트 순서 변경
    // =========================

    @PatchMapping("/portfolios/{portfolioId}/order")
    @Operation(
            summary = "포트폴리오 프로젝트 순서 변경",
            description = """
                    포트폴리오에 포함된 프로젝트들의 표시 순서를 변경합니다.
                    
                    정책:
                    - 로그인 필수
                    - 본인 포트폴리오만 수정 가능
                    - 요청한 projectOrder 배열은 현재 포트폴리오에 포함된 프로젝트 ID와 정확히 일치해야 함
                    - 누락, 중복, 추가 ID가 있으면 실패
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "프로젝트 순서 변경 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 프로젝트 순서 배열")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요함")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "포트폴리오 권한 없음")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "포트폴리오 없음")
    public ApiResponse<PortfolioOrderUpdateResponse> updateProjectOrder(
            @AuthenticatedUser Long userId,
            @PathVariable Long portfolioId,
            @Valid @RequestBody PortfolioOrderUpdateRequest request
    ) {
        PortfolioOrderUpdateResponse response = portfolioService.updateProjectOrder(
                userId,
                portfolioId,
                request
        );

        return ApiResponse.success(response);
    }
}