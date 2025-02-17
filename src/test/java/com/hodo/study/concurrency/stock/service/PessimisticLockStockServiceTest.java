package com.hodo.study.concurrency.stock.service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.hodo.study.concurrency.stock.domain.Stock;
import com.hodo.study.concurrency.stock.repository.StockRepository;

@SpringBootTest
public class PessimisticLockStockServiceTest {

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private PessimisticLockStockService stockService;


	@BeforeEach
	public void init(){
		stockRepository.deleteAll();
		Stock stock = new Stock(1L, 100);
		stockRepository.saveAndFlush(stock);
	}

	@Test
	@DisplayName("재고 감소 요청 동시 100건 테스트")
	public void decreaseHundredTest () throws InterruptedException {
		int threadCount = 100;
		final CountDownLatch latch = new CountDownLatch(threadCount);

		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

		for (int i = 0; i < threadCount; i++) {
			executorService.submit(() -> {
				try {
					stockService.decrease(1L, 1);
				}finally {
					latch.countDown();
				}

			});
		}

		latch.await();
		Stock stock = stockRepository.findById(1L).orElseThrow();

		Assertions.assertEquals(stock.getQuantity(), 0);
	}
}
