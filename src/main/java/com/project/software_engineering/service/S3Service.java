package com.project.software_engineering.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

/**
 * 파일을 AWS S3에 업로드하는 서비스
 */
@Service
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    // 파일을 S3에 업로드하고 퍼블릭 URL 반환
    public String uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            return null;
        }

        try {
            // UUID로 중복 방지
            String savedFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(savedFileName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + savedFileName;

        } catch (IOException e) {
            throw new RuntimeException("S3 파일 업로드 중 오류가 발생했습니다.", e);
        }
    }
}
