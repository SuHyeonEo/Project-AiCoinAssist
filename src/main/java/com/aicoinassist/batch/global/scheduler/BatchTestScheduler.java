package com.aicoinassist.batch.global.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BatchTestScheduler {
    @Scheduled(fixedRate = 60000)
    public void run() {
        log.info("스케줄러 테스트");
    }
}
