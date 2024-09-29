package org.test;

import org.springframework.scheduling.annotation.Scheduled;

public class Scheduler {
	
	@Scheduled(cron = "MON 10 13 * * *")
	public void invalidExp() {
		System.out.println("Scheduled task executed");
	}

}
