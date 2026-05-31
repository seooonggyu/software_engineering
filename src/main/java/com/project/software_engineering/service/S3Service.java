package com.project.software_engineering.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

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

    /**
     * MultipartFile을 S3에 업로드하고 퍼블릭 URL을 반환합니다.
     */
    public String uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            return null;
        }

        try {
            String originalFilename = file.getOriginalFilename();
            // 중복 방지를 위한 UUID 추가 (공백이나 특수문자 제거는 필요에 따라 추가 가능)
            String savedFileName = UUID.randomUUID().toString() + "_" + originalFilename;

            // S3에 파일 업로드 요청 생성
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(savedFileName)
                    .contentType(file.getContentType())
                    .build();

            // 업로드 실행
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // 업로드 완료 후, 해당 파일의 퍼블릭 S3 URL 생성
            // 예: https://software-engineering-foundit.s3.ap-northeast-2.amazonaws.com/uuid_filename.jpg
            return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + savedFileName;
            
        } catch (IOException e) {
            throw new RuntimeException("S3 파일 업로드 중 오류가 발생했습니다.", e);
        }
    }
}
