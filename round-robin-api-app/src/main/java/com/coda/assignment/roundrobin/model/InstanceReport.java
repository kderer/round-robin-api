package com.coda.assignment.roundrobin.model;

import lombok.Data;

@Data
public class InstanceReport {
	private String url;
	private int totalRequestCount;

	public InstanceReport(String url) {
		this.url = url;
	}

	public void increaseRequestCount() {
		totalRequestCount += 1;
	}
}
