package com.coda.assignment.roundrobin.model;

import com.coda.assignment.roundrobin.enumaration.InstanceStatus;

import lombok.Data;

@Data
public class ApiServerInstance {
	private String url;
	private InstanceStatus status;
	private int averageProcessTime;
	private long coolDownPeriodStart;

	public ApiServerInstance(String url) {
		this.url = url;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ApiServerInstance) {
			return ((ApiServerInstance) obj).getUrl().equals(this.url);
		}

		return false;
	}

	// This is added for the warning given by @Data
	// Either both equals and hashCode method should be implemented or none
	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
