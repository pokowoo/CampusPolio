package com.campuspolio.domain.user.controller;

import com.campuspolio.domain.user.dto.UserMeResponse;
import com.campuspolio.domain.user.service.UserService;
import com.campuspolio.global.response.ApiResponse;
import com.campuspolio.global.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserMeResponse> me(@AuthenticatedUser Long userId) {
        UserMeResponse response = userService.getMe(userId);

        return ApiResponse.success(response);
    }
}