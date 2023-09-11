package com.coda.assignment.roundrobin.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.coda.assignment.roundrobin.model.ApiServerInstance;

@Service
public class ManagementService {
	private final List<ApiServerInstance> instanceList = new ArrayList<>();

	public void addInstance(String url) {
		ApiServerInstance instance = new ApiServerInstance(url);

		if (!instanceList.contains(instance)) {
			instanceList.add(instance);
		}
	}

	public void removeInstance(String url) {
		instanceList.removeIf(instance -> instance.getUrl().equals(url));
	}

	public void removeAllInstances() {
		instanceList.clear();
	}

	public List<ApiServerInstance> getInstanceList() {
		return Collections.unmodifiableList(instanceList);
	}

	public ApiServerInstance getInstance(String url) {
		return instanceList.stream().filter(instance -> instance.getUrl().equals(url)).findAny().orElse(null);
	}

	public void setInstanceAvarageProcessTime(String url, int avarageProcessTime) {
		ApiServerInstance found = instanceList.stream().filter(instance -> instance.getUrl().equals(url)).findAny()
				.get();

		if (found != null) {
			found.setAverageProcessTime(avarageProcessTime);
		}
	}

}
