package com.campuspolio.domain.profile.service;

import com.campuspolio.domain.profile.dto.ProfileCreateRequest;
import com.campuspolio.domain.profile.dto.ProfileCreateResponse;
import com.campuspolio.domain.profile.dto.ProfileResponse;
import com.campuspolio.domain.profile.dto.ProfileUpdateRequest;
import com.campuspolio.domain.profile.dto.ProfileUpdateResponse;
import com.campuspolio.domain.profile.entity.UserProfile;
import com.campuspolio.domain.profile.repository.UserProfileRepository;
import com.campuspolio.domain.user.entity.User;
import com.campuspolio.domain.user.repository.UserRepository;
import com.campuspolio.global.exception.CustomException;
import com.campuspolio.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    public ProfileResponse getMyProfile(Long userId) {
        User user = findActiveUser(userId);

        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.PROFILE_NOT_FOUND));

        return new ProfileResponse(
                profile.getId(),
                user.getId(),
                profile.getName(),
                profile.getNickname(),
                profile.getBio(),
                profile.getMajor(),
                profile.getGrade(),
                profile.getProfileImage()
        );
    }

    @Transactional
    public ProfileCreateResponse createProfile(Long userId, ProfileCreateRequest request) {
        User user = findActiveUser(userId);

        if (userProfileRepository.existsByUserId(user.getId())) {
            throw new CustomException(ErrorCode.PROFILE_ALREADY_EXISTS);
        }

        UserProfile profile = UserProfile.create(
                user,
                request.nickname(),
                request.bio()
        );

        UserProfile savedProfile = userProfileRepository.save(profile);

        return new ProfileCreateResponse(
                savedProfile.getId(),
                "프로필 생성 완료"
        );
    }

    @Transactional
    public ProfileUpdateResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = findActiveUser(userId);

        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.PROFILE_NOT_FOUND));

        profile.update(
                request.name(),
                request.nickname(),
                request.bio(),
                request.major(),
                request.grade(),
                request.profileImage()
        );

        return new ProfileUpdateResponse(
                user.getId(),
                profile.getUpdatedAt(),
                "프로필 수정 완료"
        );
    }

    private User findActiveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.isDeleted()) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        return user;
    }
}