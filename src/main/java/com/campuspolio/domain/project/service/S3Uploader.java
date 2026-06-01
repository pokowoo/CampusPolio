package com.campuspolio.domain.project.service;

import com.campuspolio.global.exception.CustomException;
import com.campuspolio.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3Uploader {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String upload(
            Long projectId,
            MultipartFile file
    ) {

        try {

            String key =
                    "projects/"
                            + projectId
                            + "/"
                            + UUID.randomUUID()
                            + "-"
                            + file.getOriginalFilename();

            PutObjectRequest request =
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(
                                    file.getContentType()
                            )
                            .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromBytes(
                            file.getBytes()
                    )
            );

            return String.format(
                    "https://%s.s3.%s.amazonaws.com/%s",
                    bucket,
                    s3Client.serviceClientConfiguration()
                            .region()
                            .id(),
                    key
            );

        } catch (IOException e) {

            throw new CustomException(
                    ErrorCode.S3_UPLOAD_FAILED
            );
        }
    }
}