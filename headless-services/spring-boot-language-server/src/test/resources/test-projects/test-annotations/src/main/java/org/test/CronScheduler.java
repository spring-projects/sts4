package org.test;

import org.springframework.scheduling.annotation.Scheduled;

public class CronScheduler {

	@Scheduled(cron = "")
	public void cronCompletionTest() {
	}
	
	@Scheduled(cron = "0 0 * * * *")
	public void performTask1() {
		System.out.println("Scheduled task executed");
	}

	@Scheduled(cron = "1 * * * * *")
	public void performTask2() {
		System.out.println("Scheduled task executed");
	}

	@Scheduled(cron = "10 * * * * *")
	public void performTask3() {
		System.out.println("Scheduled task executed");
	}

	@Scheduled(cron = "0 0 8-10 * * *")
	public void performTask4() {
		System.out.println("Scheduled task executed");
	}

	@Scheduled(cron = "0 0 6,19 * * *")
	public void performTask5() {
		System.out.println("Scheduled task executed");
	}

	@Scheduled(cron = "0 0/30 8-10 * * *")
	public void performTask6() {
		System.out.println("Scheduled task executed");
	}

	@Scheduled(cron = "0 0 9-17 * * MON-FRI")
	public void performTask7() {
		System.out.println("Scheduled task executed");
	}

	@Scheduled(cron = "0 0 * * * 1-5")
	public void performTask8() {
		System.out.println("Scheduled task executed");
	}

	@Scheduled(cron = "0-59 10 13 * * *", zone = "UTC", fixedRate = 5000, initialDelay
		= 1000)
	public void performTask9() {
		System.out.println("Scheduled task executed");
	}
}
