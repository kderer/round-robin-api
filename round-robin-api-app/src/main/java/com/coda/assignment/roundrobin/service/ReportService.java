package com.coda.assignment.roundrobin.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.coda.assignment.roundrobin.model.InstanceReport;

@Service
public class ReportService {

	private final List<InstanceReport> reportList = new ArrayList<>();

	public List<InstanceReport> getInstanceInstanceReportList() {
		return Collections.unmodifiableList(reportList);
	}

	public InstanceReport getInstanceReport(String url) {
		return reportList.stream().filter(instance -> instance.getUrl().equals(url)).findAny().orElse(null);
	}

	public InstanceReport getOrCreateInstanceReport(String url) {
		InstanceReport instanceReport = reportList.stream().filter(instance -> instance.getUrl().equals(url)).findAny()
				.orElse(null);

		if (instanceReport == null) {
			instanceReport = new InstanceReport(url);
			reportList.add(instanceReport);
		}

		return instanceReport;
	}

	public void removeAllReports() {
		reportList.clear();
	}

}
