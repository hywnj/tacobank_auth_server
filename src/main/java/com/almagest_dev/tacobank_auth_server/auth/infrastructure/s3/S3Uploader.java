package com.almagest_dev.tacobank_auth_server.auth.infrastructure.s3;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.nio.file.Files;
import java.util.Objects;

public class S3Uploader {
    private static final String BUCKET_NAME = "taco-bank-logs";
    private static final String S3_FOLDER = "logs";
    private static final String LOCAL_LOG_PATH = "/log";

    private final S3Client s3Client;

    public S3Uploader() {
        this.s3Client = S3Client.create(); // 기본 AWS 환경에서 인증
    }

    public void uploadLogsToS3() {
        try {
            File logDir = new File(LOCAL_LOG_PATH);
            if (!logDir.exists() || !logDir.isDirectory()) {
                System.err.println("Log directory does not exist or is not a directory.");
                return;
            }

            for (File logFile : Objects.requireNonNull(logDir.listFiles())) {
                if (logFile.isFile()) {
                    String s3Key = String.format("%s/%s", S3_FOLDER, logFile.getName());
                    s3Client.putObject(
                            PutObjectRequest.builder()
                                    .bucket(BUCKET_NAME)
                                    .key(s3Key)
                                    .build(),
                            logFile.toPath()
                    );
                    System.out.println("Uploaded: " + logFile.getName());
                    Files.delete(logFile.toPath());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
