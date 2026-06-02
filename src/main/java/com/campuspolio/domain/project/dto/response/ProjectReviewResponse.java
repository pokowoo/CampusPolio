package com.campuspolio.domain.project.dto.response;

import java.util.List;

public record ProjectReviewResponse(

        Integer totalScore,

        String summary,

        List<ReviewCategory> categories,

        List<ReviewIssue> criticalIssues,

        List<ReviewIssue> warnings,

        List<ReviewStrength> strengths,

        List<String> interviewQuestions,

        List<RefactoringSuggestion> refactoringSuggestions

) {
}