package com.coda.assignment.roundrobin.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coda.assignment.roundrobin.model.InstanceReport;
import com.coda.assignment.roundrobin.service.ReportService;

@RestController
@RequestMapping("/report")
public class ReportController {

	@Autowired
	private ReportService reportService;

	@GetMapping
	public List<InstanceReport> queryAll() {
		return reportService.getInstanceInstanceReportList();
	}

	@GetMapping(value = "/{url}")
	public InstanceReport queryReportForInstance(@PathVariable String url) {
		return reportService.getInstanceReport("http://" + url);
	}

}
