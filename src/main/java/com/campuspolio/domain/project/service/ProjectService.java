package com.campuspolio.domain.project.service;

import com.campuspolio.domain.project.dto.request.ProjectCreateRequest;
import com.campuspolio.domain.project.dto.response.ProjectCreateResponse;
import com.campuspolio.domain.project.entity.Project;
import com.campuspolio.domain.project.entity.ProjectMember;
import com.campuspolio.domain.project.repository.ProjectMemberRepository;
import com.campuspolio.domain.project.repository.ProjectRepository;
import com.campuspolio.domain.user.entity.User;
import com.campuspolio.domain.user.repository.UserRepository;
import com.campuspolio.global.exception.CustomException;
import com.campuspolio.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    public ProjectCreateResponse createProject(
            Long loginUserId,
            ProjectCreateRequest request
    ) {

        User user = userRepository.findById(loginUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // draft 프로젝트 생성
        Project project = Project.createDraft(
                request.title(),
                request.description()
        );

        projectRepository.save(project);

        // owner 생성
        ProjectMember owner = ProjectMember.createOwner(
                project,
                user
        );

        projectMemberRepository.save(owner);

        return new ProjectCreateResponse(
                project.getProjectId(),
                project.getStatus()
        );
    }
}