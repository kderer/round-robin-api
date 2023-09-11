package com.coda.assignment.roundrobin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coda.assignment.roundrobin.service.ReportService;

@RestController
@RequestMapping("/report")
public class ReportController {

	@Autowired
	private ReportService reportService;

	@GetMapping
	public void queryAll() {
		reportService.getInstanceInstanceReportList();
	}

	@GetMapping(value = "/{url}")
	public void queryReportForInstance(@PathVariable String url) {
		reportService.getInstanceReport(url);
	}

}
