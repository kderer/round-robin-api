package com.coda.assignment.roundrobin.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coda.assignment.roundrobin.model.ApiServerInstance;
import com.coda.assignment.roundrobin.service.ManagementService;

@RestController
@RequestMapping("/manage/server")
public class ManagementController {

	@Autowired
	private ManagementService managementService;

	@PostMapping
	public void addInstance(String url) {
		if (!url.startsWith("http://")) {
			url = "http://" + url;
		}

		managementService.addInstance(url);
	}

	@DeleteMapping
	public void removeInstance(String url) {
		if (!url.startsWith("http://")) {
			url = "http://" + url;
		}

		managementService.removeInstance(url);
	}

	@GetMapping
	public List<ApiServerInstance> list() {
		return managementService.getInstanceList();
	}

	@GetMapping("/{url}")
	public ApiServerInstance getServerInfo(@PathVariable String url) {
		return managementService.getInstance("http://" + url);
	}

	@PutMapping("/{url}/apt/{apt}")
	public void updateInstanceApt(@PathVariable String url, @PathVariable Integer apt) {
		managementService.setInstanceAvarageProcessTime("http://" + url, apt);
	}

}
