package com.almagest_dev.tacobank_auth_server.auth.infrastructure.s3;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LogScheduler {

    private final S3Uploader s3Uploader;

    public LogScheduler(S3Uploader s3Uploader) {
        this.s3Uploader = s3Uploader;
    }

    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정 실행
    public void scheduleLogUpload() {
        System.out.println("Starting daily log upload to S3...");
        s3Uploader.uploadLogsToS3();
        System.out.println("Daily log upload to S3 completed.");
    }
}
