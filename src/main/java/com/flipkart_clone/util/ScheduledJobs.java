package com.flipkart_clone.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.flipkart_clone.serviceimplementation.AuthServiceImplementation;

@Component
public class ScheduledJobs {
	
	@Autowired
	private AuthServiceImplementation authServiceImplementation;
	
//	@Scheduled(cron = " 0 0  0 * * * ")
	public void callCleanupUnVerifiedUsers() {
		authServiceImplementation.cleanupUnverifiedUsers();
		System.out.println("Users deleted");
	}

}
