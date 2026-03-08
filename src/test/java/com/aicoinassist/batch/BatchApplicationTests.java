package com.aicoinassist.batch;

import com.aicoinassist.batch.global.scheduler.MarketRawIngestionScheduler;
import com.aicoinassist.batch.global.scheduler.AnalysisReportBatchScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BatchApplicationTests {

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	void contextLoadsWithoutSchedulers() {
		assertThat(applicationContext.getBeansOfType(MarketRawIngestionScheduler.class)).isEmpty();
		assertThat(applicationContext.getBeansOfType(AnalysisReportBatchScheduler.class)).isEmpty();
	}

}
