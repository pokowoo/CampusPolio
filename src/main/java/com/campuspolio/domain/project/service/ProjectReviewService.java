package com.campuspolio.domain.project.service;

import com.campuspolio.domain.project.dto.response.ProjectReviewResponse;
import com.campuspolio.domain.project.entity.Project;
import com.campuspolio.domain.project.entity.ProjectFile;
import com.campuspolio.domain.project.repository.ProjectFileRepository;
import com.campuspolio.domain.project.repository.ProjectRepository;
import com.campuspolio.global.claude.ClaudeClient;
import com.campuspolio.global.exception.CustomException;
import com.campuspolio.global.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectReviewService {

    private final ProjectRepository projectRepository;
    private final ProjectFileRepository projectFileRepository;
    private final ClaudeClient claudeClient;
    private final ObjectMapper objectMapper;

    private static final Set<String> CODE_EXTENSIONS =
            Set.of(
                    "java",
                    "js",
                    "ts",
                    "html",
                    "css",
                    "py",
                    "sql",
                    "xml",
                    "json",
                    "yml",
                    "yaml",
                    "properties",
                    "gradle",
                    "md"
            );

    public ProjectReviewResponse reviewProject(
            Long projectId
    ) {

        Project project =
                projectRepository
                        .findByIdAndDeletedAtIsNull(projectId)
                        .orElseThrow(() ->
                                new CustomException(
                                        ErrorCode.PROJECT_NOT_FOUND
                                )
                        );

        List<ProjectFile> files =
                projectFileRepository
                        .findAllByProjectAndDeletedAtIsNull(
                                project
                        );

        List<ProjectFile> codeFiles =
                files.stream()
                        .filter(this::isCodeFile)
                        .toList();

        if (codeFiles.isEmpty()) {

            throw new CustomException(
                    ErrorCode.PROJECT_REVIEW_FILE_NOT_FOUND
            );
        }

        StringBuilder prompt =
                new StringBuilder();

        prompt.append("""
당신은 네이버·카카오·토스 수준의 시니어 소프트웨어 엔지니어입니다.

프로젝트 코드를 리뷰하세요.

반드시 JSON만 응답하세요.

{
  "totalScore": 0,
  "summary": "",
  "categories": [
    {
      "category": "CODE_STYLE",
      "score": 0,
      "comment": ""
    }
  ],
  "criticalIssues": [
    {
      "title": "",
      "description": "",
      "solution": ""
    }
  ],
  "warnings": [
    {
      "title": "",
      "description": "",
      "solution": ""
    }
  ],
  "strengths": [
    {
      "title": "",
      "description": ""
    }
  ],
  "interviewQuestions": [
    ""
  ],
  "refactoringSuggestions": [
    {
      "title": "",
      "description": "",
      "exampleCode": ""
    }
  ]
}

category 값은 반드시 아래 값 중 하나만 사용하세요.

CODE_STYLE
READABILITY
EXCEPTION_HANDLING
LAYERED_ARCHITECTURE
OBJECT_ORIENTED_DESIGN
SCALABILITY

categories는 반드시 정확히 6개를 포함하세요.

JSON 외의 설명은 절대 출력하지 마세요.
각 항목은 3문장 이하로 작성하세요.
exampleCode는 최대 10줄만 작성하세요.
전체 응답은 3000자 이내로 작성하세요.
""");

        for (ProjectFile file : codeFiles) {

            String content =
                    downloadFile(
                            file.getFileUrl()
                    );

            if (content.length() > 20000) {

                content =
                        content.substring(
                                0,
                                20000
                        );

                content +=
                        "\n\n[파일이 너무 커서 일부만 분석함]";
            }

            prompt.append("\n\n");
            prompt.append("=== ");
            prompt.append(file.getOriginalName());
            prompt.append(" ===\n");
            prompt.append(content);
        }

        String review =
                claudeClient.review(
                        prompt.toString()
                );

        review = review
                .replace("```json", "")
                .replace("```", "")
                .trim();

        //log.info("===== Claude Review Response =====");
        //log.info(review);
        //log.info("==================================");

        try {

            return objectMapper.readValue(
                    review,
                    ProjectReviewResponse.class
            );

        } catch (JsonProcessingException e) {

            log.error(
                    "Claude 응답 파싱 실패\n응답 내용 : {}",
                    //review,
                    e
            );

            throw new CustomException(
                    ErrorCode.PROJECT_REVIEW_PARSE_ERROR
            );
        }
    }

    private boolean isCodeFile(
            ProjectFile file
    ) {

        String fileName =
                file.getOriginalName()
                        .toLowerCase();

        int index =
                fileName.lastIndexOf(".");

        if (index == -1) {
            return false;
        }

        String ext =
                fileName.substring(index + 1);

        return CODE_EXTENSIONS.contains(ext);
    }

    private String downloadFile(
            String url
    ) {

        RestTemplate restTemplate =
                new RestTemplate();

        return restTemplate.getForObject(
                url,
                String.class
        );
    }
}