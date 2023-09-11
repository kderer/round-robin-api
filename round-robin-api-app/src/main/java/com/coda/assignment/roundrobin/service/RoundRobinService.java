package com.coda.assignment.roundrobin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.coda.assignment.roundrobin.enumaration.InstanceStatus;
import com.coda.assignment.roundrobin.exception.NoAvailableInstanceException;
import com.coda.assignment.roundrobin.model.ApiServerInstance;

@Service
public class RoundRobinService {
	@Autowired
	private ManagementService managementService;

	private final RestTemplate restTemplate = new RestTemplate();

	private int instanceCounter;

	@Scheduled(fixedDelay = 100)
	private void healthCheck() {
		managementService.getInstanceList().forEach(instance -> {
			try {
				instance.setStatus(
						InstanceStatus.valueOf(restTemplate.getForObject(instance.getUrl() + "/health", String.class)));
			} catch (Exception e) {
				instance.setStatus(InstanceStatus.DOWN);
			}
		});
	}

	/**
	 * Computes which instance to serve using a Round Robin approach
	 * <p>
	 * If the current instance is down or slow performance, picks the next OK server
	 * </p>
	 * <p>
	 * If both current instance and backup server are slow, picks the less slow one
	 * </p>
	 * 
	 * @return
	 * @throws NoAvailableInstanceException
	 */
	public synchronized ApiServerInstance getApiServerInstance() throws NoAvailableInstanceException {
		if (managementService.getInstanceList().size() == 0) {
			throw new NoAvailableInstanceException();
		}

		instanceCounter = instanceCounter % managementService.getInstanceList().size();

		ApiServerInstance instance = managementService.getInstanceList().get(instanceCounter);

		instanceCounter += 1;

		if (instance.getStatus() == InstanceStatus.OK) {
			return instance;
		}

		// Find a backup instance if the current instance is down or slow
		int tempCounter = instanceCounter;
		ApiServerInstance tempInstance = null;
		ApiServerInstance backupInstance = null;

		// Iterate over the next instances for finding out an OK status instance
		// If there is no OK status instance, the first SLOW status instance will be
		// picked up as backup instance
		for (int i = 0; i < managementService.getInstanceList().size(); i++) {
			tempCounter = tempCounter % managementService.getInstanceList().size();
			tempInstance = managementService.getInstanceList().get(tempCounter);

			tempCounter += 1;

			if (tempInstance.getStatus() == InstanceStatus.OK) {
				backupInstance = tempInstance;
				break;
			}

			if (backupInstance == null && tempInstance.getStatus() == InstanceStatus.SLOW) {
				backupInstance = tempInstance;
			}
		}

		if (instance.getStatus() == InstanceStatus.DOWN) {
			// If all the instances are DOWN, NoAvailableInstanceException is thrown
			if (backupInstance == null) {
				throw new NoAvailableInstanceException();
			}

			instanceCounter = tempCounter;

			return backupInstance;
		}

		// If both instances are SLOW, pick the less slow one
		if (backupInstance.getStatus() == InstanceStatus.SLOW
				&& instance.getAverageProcessTime() < backupInstance.getAverageProcessTime()) {
			return instance;
		}

		instanceCounter = tempCounter;

		return backupInstance;
	}
	
	public void resetInstanceCounter() {
		instanceCounter = 0;
	}

}
