package com.campuspolio.domain.user.controller;

import com.campuspolio.domain.user.dto.UserMeResponse;
import com.campuspolio.domain.user.service.UserService;
import com.campuspolio.global.exception.CustomException;
import com.campuspolio.global.exception.ErrorCode;
import com.campuspolio.global.response.ApiResponse;
import com.campuspolio.global.security.SessionConst;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserMeResponse> me(HttpSession session) {
        Long userId = (Long) session.getAttribute(SessionConst.LOGIN_USER_ID);

        if (userId == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        UserMeResponse response = userService.getMe(userId);

        return ApiResponse.success(response);
    }
}