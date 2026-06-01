package com.campuspolio.domain.portfolio.service;

import com.campuspolio.domain.portfolio.dto.*;
import com.campuspolio.domain.portfolio.entity.Portfolio;
import com.campuspolio.domain.portfolio.entity.PortfolioProject;
import com.campuspolio.domain.portfolio.repository.PortfolioProjectRepository;
import com.campuspolio.domain.portfolio.repository.PortfolioRepository;
import com.campuspolio.domain.project.entity.Project;
import com.campuspolio.domain.project.entity.ProjectStatus;
import com.campuspolio.domain.project.repository.UserProjectRepository;
import com.campuspolio.domain.project.repository.ProjectRepository;
import com.campuspolio.domain.user.entity.User;
import com.campuspolio.domain.user.repository.UserRepository;
import com.campuspolio.global.exception.CustomException;
import com.campuspolio.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioService {

    private static final int DEFAULT_PORTFOLIO_PAGE_SIZE = 6;
    private static final int MAX_PORTFOLIO_PROJECT_COUNT = 50;

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final UserProjectRepository userProjectRepository;
    private final PortfolioRepository portfolioRepository;
    private final PortfolioProjectRepository portfolioProjectRepository;

    // =========================
    // 포트폴리오 생성
    // =========================

    @Transactional
    public PortfolioCreateResponse createPortfolio(
            Long userId,
            PortfolioCreateRequest request
    ) {
        User user = findActiveUser(userId);

        // 정책: 포트폴리오 생성은 대학 인증 완료 사용자만 가능
        if (!user.isUniversityVerified()) {
            throw new CustomException(ErrorCode.UNIVERSITY_VERIFICATION_REQUIRED);
        }

        String title = request.title().trim();

        if (title.isBlank()) {
            throw new CustomException(ErrorCode.PORTFOLIO_TITLE_REQUIRED);
        }

        String slug = generateUniqueSlug(title);

        Portfolio portfolio = Portfolio.create(
                user,
                title,
                slug
        );

        Portfolio savedPortfolio = portfolioRepository.save(portfolio);

        return new PortfolioCreateResponse(
                savedPortfolio.getPortfolioId(),
                savedPortfolio.getSlug()
        );
    }

    // =========================
    // 내 포트폴리오 목록 조회
    // =========================

    public MyPortfolioListResponse getMyPortfolios(
            Long userId,
            int page,
            int size,
            Boolean isPublic
    ) {
        User user = findActiveUser(userId);

        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? DEFAULT_PORTFOLIO_PAGE_SIZE : size;

        Pageable pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Direction.DESC, "updatedAt")
        );

        Page<Portfolio> portfolioPage;

        /*
         * isPublic 파라미터가 없으면 전체 조회,
         * true/false가 들어오면 공개 여부에 따라 필터링한다.
         */
        if (isPublic == null) {
            portfolioPage = portfolioRepository.findAllByUserAndDeletedAtIsNull(
                    user,
                    pageable
            );
        } else {
            portfolioPage = portfolioRepository.findAllByUserAndIsPublicAndDeletedAtIsNull(
                    user,
                    isPublic,
                    pageable
            );
        }

        List<MyPortfolioResponse> content = portfolioPage.getContent()
                .stream()
                .map(this::toMyPortfolioResponse)
                .toList();

        return new MyPortfolioListResponse(
                content,
                portfolioPage.getNumber(),
                portfolioPage.getSize(),
                portfolioPage.getTotalElements(),
                portfolioPage.getTotalPages()
        );
    }

    // =========================
    // slug 기반 포트폴리오 상세 조회
    // =========================

    public PortfolioDetailResponse getPortfolioBySlug(
            String slug,
            Long loginUserId
    ) {
        Portfolio portfolio = portfolioRepository.findBySlugAndDeletedAtIsNull(slug)
                .orElseThrow(() -> new CustomException(ErrorCode.PORTFOLIO_NOT_FOUND));

        /*
         * 정책:
         * 공개 포트폴리오: 비로그인도 접근 가능
         * 비공개 포트폴리오: 본인만 접근 가능
         * 타인 접근 시 404 반환
         */
        if (!portfolio.isPublic()) {
            if (loginUserId == null) {
                throw new CustomException(ErrorCode.PORTFOLIO_NOT_FOUND);
            }

            if (!portfolio.getUser().getId().equals(loginUserId)) {
                throw new CustomException(ErrorCode.PORTFOLIO_NOT_FOUND);
            }
        }

        /*
         * 정책:
         * 포트폴리오 상세에서는 공개 + 발행 + 삭제되지 않은 프로젝트만 노출
         */
        List<PortfolioDetailProjectResponse> projects =
                portfolioProjectRepository.findAllByPortfolioOrderByDisplayOrderAsc(portfolio)
                        .stream()
                        .filter(portfolioProject -> isVisiblePortfolioProject(portfolioProject.getProject()))
                        .map(this::toPortfolioDetailProjectResponse)
                        .toList();

        return new PortfolioDetailResponse(
                portfolio.getPortfolioId(),
                portfolio.getTitle(),
                portfolio.getSlug(),
                portfolio.getDescription(),
                portfolio.getThumbnailUrl(),
                portfolio.isPublic(),
                projects
        );
    }

    // =========================
    // 포트폴리오 정보 수정
    // =========================

    @Transactional
    public PortfolioUpdateResponse updatePortfolio(
            Long userId,
            Long portfolioId,
            PortfolioUpdateRequest request
    ) {
        User user = findActiveUser(userId);
        Portfolio portfolio = findActivePortfolio(portfolioId);

        // 정책: 포트폴리오 수정은 본인만 가능
        validatePortfolioOwner(portfolio, user);

        String title = resolveTitle(
                portfolio.getTitle(),
                request.title()
        );

        String description = request.description() == null
                ? portfolio.getDescription()
                : request.description();

        String thumbnailUrl = request.thumbnailUrl() == null
                ? portfolio.getThumbnailUrl()
                : request.thumbnailUrl();

        /*
         * 정책:
         * 제목을 수정해도 slug는 변경하지 않는다.
         */
        portfolio.updateInfo(
                title,
                description,
                thumbnailUrl
        );

        return new PortfolioUpdateResponse(
                portfolio.getPortfolioId(),
                portfolio.getUpdatedAt(),
                "포트폴리오 수정 완료"
        );
    }

    // =========================
    // 포트폴리오 공개 / 비공개 변경
    // =========================

    @Transactional
    public PortfolioVisibilityUpdateResponse updateVisibility(
            Long userId,
            Long portfolioId,
            PortfolioVisibilityUpdateRequest request
    ) {
        User user = findActiveUser(userId);
        Portfolio portfolio = findActivePortfolio(portfolioId);

        // 정책: 포트폴리오 공개/비공개 변경은 본인만 가능
        validatePortfolioOwner(portfolio, user);

        if (Boolean.TRUE.equals(request.isPublic())) {
            /*
             * 정책:
             * 공개 포트폴리오는 공개 + 발행 + 삭제되지 않은 프로젝트를 1개 이상 포함해야 한다.
             */
            if (!hasVisibleProject(portfolio)) {
                throw new CustomException(ErrorCode.PORTFOLIO_PUBLIC_PROJECT_REQUIRED);
            }

            portfolio.makePublic();
        } else {
            portfolio.makePrivate();
        }

        return new PortfolioVisibilityUpdateResponse(
                portfolio.getPortfolioId(),
                portfolio.isPublic(),
                "포트폴리오 공개 상태 변경 완료"
        );
    }

    // =========================
    // 포트폴리오 삭제
    // =========================

    @Transactional
    public PortfolioDeleteResponse deletePortfolio(
            Long userId,
            Long portfolioId
    ) {
        User user = findActiveUser(userId);
        Portfolio portfolio = findActivePortfolio(portfolioId);

        // 정책: 포트폴리오 삭제는 본인만 가능
        validatePortfolioOwner(portfolio, user);

        /*
         * 정책:
         * 포트폴리오 삭제 시 프로젝트 자체는 유지하고,
         * 포트폴리오-프로젝트 연결만 제거한다.
         */
        portfolioProjectRepository.deleteAllByPortfolio(portfolio);

        /*
         * 정책:
         * 포트폴리오는 soft delete 처리한다.
         * slug는 DB row가 남아있으므로 재사용되지 않는다.
         */
        portfolio.delete();

        return new PortfolioDeleteResponse("포트폴리오 삭제 완료");
    }

    // =========================
    // 포트폴리오 프로젝트 추가 / 제거
    // =========================

    @Transactional
    public PortfolioProjectUpdateResponse updatePortfolioProjects(
            Long userId,
            Long portfolioId,
            PortfolioProjectUpdateRequest request
    ) {
        User user = findActiveUser(userId);
        Portfolio portfolio = findActivePortfolio(portfolioId);

        // 정책: 포트폴리오 프로젝트 구성 변경은 본인만 가능
        validatePortfolioOwner(portfolio, user);

        List<Long> removeProjectIds = distinctIds(request.remove());
        List<Long> addProjectIds = distinctIds(request.add());

        // 1. 제거 먼저 처리
        for (Long projectId : removeProjectIds) {
            Project project = findActiveProject(projectId);

            PortfolioProject portfolioProject =
                    portfolioProjectRepository.findByPortfolioAndProject(portfolio, project)
                            .orElseThrow(() -> new CustomException(ErrorCode.PORTFOLIO_PROJECT_NOT_INCLUDED));

            portfolioProjectRepository.delete(portfolioProject);
        }

        // 2. 최대 50개 제한 검증
        long currentCount = portfolioProjectRepository.countByPortfolio(portfolio);

        if (currentCount + addProjectIds.size() > MAX_PORTFOLIO_PROJECT_COUNT) {
            throw new CustomException(ErrorCode.PORTFOLIO_PROJECT_LIMIT_EXCEEDED);
        }

        int nextOrder = getNextDisplayOrder(portfolio);

        // 3. 추가 처리
        for (Long projectId : addProjectIds) {
            Project project = findActiveProject(projectId);

            // 정책: 본인이 OWNER 또는 MEMBER인 프로젝트만 추가 가능
            validateProjectAccessibleByUser(project, user);

            if (portfolioProjectRepository.existsByPortfolioAndProject(portfolio, project)) {
                throw new CustomException(ErrorCode.PORTFOLIO_PROJECT_DUPLICATED);
            }

            PortfolioProject portfolioProject = PortfolioProject.create(
                    portfolio,
                    project,
                    nextOrder++
            );

            portfolioProjectRepository.save(portfolioProject);
        }

        /*
         * 정책:
         * 포함된 프로젝트가 모두 비공개이거나 발행되지 않았거나 삭제된 경우
         * 포트폴리오는 자동 비공개 처리
         */
        if (portfolio.isPublic() && !hasVisibleProject(portfolio)) {
            portfolio.makePrivate();
        }

        return new PortfolioProjectUpdateResponse("포트폴리오 프로젝트 수정 완료");
    }

    // =========================
    // 포트폴리오 프로젝트 순서 변경
    // =========================

    @Transactional
    public PortfolioOrderUpdateResponse updateProjectOrder(
            Long userId,
            Long portfolioId,
            PortfolioOrderUpdateRequest request
    ) {
        User user = findActiveUser(userId);
        Portfolio portfolio = findActivePortfolio(portfolioId);

        // 정책: 포트폴리오 순서 변경은 본인만 가능
        validatePortfolioOwner(portfolio, user);

        List<PortfolioProject> portfolioProjects =
                portfolioProjectRepository.findAllByPortfolioOrderByDisplayOrderAsc(portfolio);

        List<Long> currentProjectIds = portfolioProjects.stream()
                .map(portfolioProject -> portfolioProject.getProject().getId())
                .toList();

        List<Long> requestedProjectIds = request.projectOrder();

        validateProjectOrder(
                currentProjectIds,
                requestedProjectIds
        );

        Map<Long, PortfolioProject> portfolioProjectMap = portfolioProjects.stream()
                .collect(Collectors.toMap(
                        portfolioProject -> portfolioProject.getProject().getId(),
                        portfolioProject -> portfolioProject
                ));

        for (int i = 0; i < requestedProjectIds.size(); i++) {
            Long projectId = requestedProjectIds.get(i);
            PortfolioProject portfolioProject = portfolioProjectMap.get(projectId);
            portfolioProject.changeOrder(i);
        }

        return new PortfolioOrderUpdateResponse("프로젝트 순서 변경 완료");
    }

    // =========================
    // 공통 조회 메서드
    // =========================

    private User findActiveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.isDeleted()) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        return user;
    }

    private Portfolio findActivePortfolio(Long portfolioId) {
        return portfolioRepository.findByPortfolioIdAndDeletedAtIsNull(portfolioId)
                .orElseThrow(() -> new CustomException(ErrorCode.PORTFOLIO_NOT_FOUND));
    }

    private Project findActiveProject(Long projectId) {
        return projectRepository.findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));
    }

    // =========================
    // 권한 검증
    // =========================

    private void validatePortfolioOwner(
            Portfolio portfolio,
            User user
    ) {
        if (!portfolio.isOwner(user)) {
            throw new CustomException(ErrorCode.PORTFOLIO_FORBIDDEN);
        }
    }

    /*
     * 정책:
     * 포트폴리오에는 본인이 OWNER 또는 MEMBER인 프로젝트만 추가 가능.
     * 현재 project_members 테이블에 OWNER와 MEMBER가 모두 저장되므로,
     * ProjectMember 존재 여부로 접근 권한을 판단한다.
     */
    private void validateProjectAccessibleByUser(
            Project project,
            User user
    ) {
        boolean accessible = userProjectRepository
                .findByUserAndProject(user, project)
                .isPresent();

        if (!accessible) {
            throw new CustomException(ErrorCode.PROJECT_FORBIDDEN);
        }
    }

    private void validateProjectOrder(
            List<Long> currentProjectIds,
            List<Long> requestedProjectIds
    ) {
        if (requestedProjectIds == null || requestedProjectIds.isEmpty()) {
            throw new CustomException(ErrorCode.PORTFOLIO_PROJECT_NOT_INCLUDED);
        }

        Set<Long> currentSet = new HashSet<>(currentProjectIds);
        Set<Long> requestedSet = new HashSet<>(requestedProjectIds);

        boolean sameSize = currentProjectIds.size() == requestedProjectIds.size();
        boolean sameElements = currentSet.equals(requestedSet);
        boolean hasDuplicate = requestedProjectIds.size() != requestedSet.size();

        if (!sameSize || !sameElements || hasDuplicate) {
            throw new CustomException(ErrorCode.PORTFOLIO_PROJECT_NOT_INCLUDED);
        }
    }

    // =========================
    // 값 검증 / 변환
    // =========================

    private String resolveTitle(
            String currentTitle,
            String requestedTitle
    ) {
        if (requestedTitle == null) {
            return currentTitle;
        }

        String trimmedTitle = requestedTitle.trim();

        if (trimmedTitle.isBlank()) {
            throw new CustomException(ErrorCode.PORTFOLIO_TITLE_REQUIRED);
        }

        return trimmedTitle;
    }

    // =========================
    // slug 생성
    // =========================

    private String generateUniqueSlug(String title) {
        String baseSlug = normalizeSlug(title);

        String slug = baseSlug;
        int count = 1;

        /*
         * 정책:
         * 삭제된 slug도 재사용 불가.
         * 따라서 deletedAt 여부와 관계없이 existsBySlug로 검사한다.
         */
        while (portfolioRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + count;
            count++;
        }

        return slug;
    }

    private String normalizeSlug(String title) {
        String normalized = Normalizer.normalize(title, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");

        if (normalized.isBlank()) {
            return "portfolio";
        }

        return normalized;
    }

    // =========================
    // 포트폴리오 프로젝트 유틸
    // =========================

    private int getNextDisplayOrder(Portfolio portfolio) {
        return portfolioProjectRepository.findAllByPortfolioOrderByDisplayOrderAsc(portfolio)
                .stream()
                .mapToInt(PortfolioProject::getDisplayOrder)
                .max()
                .orElse(-1) + 1;
    }

    private boolean hasVisibleProject(Portfolio portfolio) {
        return portfolioProjectRepository.findAllByPortfolioOrderByDisplayOrderAsc(portfolio)
                .stream()
                .anyMatch(portfolioProject -> isVisiblePortfolioProject(portfolioProject.getProject()));
    }

    /*
     * 포트폴리오에서 사용자에게 노출 가능한 프로젝트 조건:
     * 1. 삭제되지 않음
     * 2. 공개 상태
     * 3. PUBLISHED 상태
     */
    private boolean isVisiblePortfolioProject(Project project) {
        return !project.isDeleted()
                && project.isPublic()
                && project.getStatus() == ProjectStatus.PUBLISHED;
    }

    private List<Long> distinctIds(List<Long> ids) {
        if (ids == null) {
            return List.of();
        }

        return ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    // =========================
    // DTO 변환
    // =========================

    private MyPortfolioResponse toMyPortfolioResponse(Portfolio portfolio) {
        long projectCount = portfolioProjectRepository.countByPortfolio(portfolio);

        return new MyPortfolioResponse(
                portfolio.getPortfolioId(),
                portfolio.getTitle(),
                portfolio.getSlug(),
                portfolio.getDescription(),
                portfolio.getThumbnailUrl(),
                portfolio.isPublic(),
                projectCount,
                portfolio.getCreatedAt(),
                portfolio.getUpdatedAt()
        );
    }

    private PortfolioDetailProjectResponse toPortfolioDetailProjectResponse(
            PortfolioProject portfolioProject
    ) {
        Project project = portfolioProject.getProject();

        return new PortfolioDetailProjectResponse(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getThumbnail(),
                portfolioProject.getDisplayOrder()
        );
    }
}