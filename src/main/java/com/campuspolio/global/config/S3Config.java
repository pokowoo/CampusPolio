package com.campuspolio.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import org.springframework.boot.CommandLineRunner;
import software.amazon.awssdk.services.sts.StsClient;
@Configuration
public class S3Config {

    @Value("${cloud.aws.region}")
    private String region;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .httpClientBuilder(
                        UrlConnectionHttpClient.builder()
                )
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
    @Bean
    public CommandLineRunner checkAwsIdentity() {
        return args -> {
            try (
                    var credentials = DefaultCredentialsProvider.create();
                    var sts = StsClient.builder()
                            .region(Region.of(region))
                            .credentialsProvider(credentials)
                            .build()
            ) {
                var identity = sts.getCallerIdentity();

                System.out.println("========== AWS IDENTITY ==========");
                System.out.println("AWS Account: " + identity.account());
                System.out.println("AWS ARN: " + identity.arn());
                System.out.println("==================================");
            }
        };
    }
}