package com.coda.assignment.roundrobin.service;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.coda.assignment.roundrobin.enumaration.InstanceStatus;
import com.coda.assignment.roundrobin.exception.NoAvailableInstanceException;
import com.coda.assignment.roundrobin.model.ApiServerInstance;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class ApiService {
	private RestTemplate restTemplate;

	@Autowired
	private RoundRobinService roundRobinService;

	@Autowired
	private ReportService reportService;

	@Autowired
	private ManagementService managementService;

	@Value("${api.server.instance.max.process.time}")
	private double maxProcessTime;

	@Value("${api.server.instance.cool.down.period}")
	private int coolDownPeriod;

	ParameterizedTypeReference<LinkedHashMap<String, Object>> responseBodyTypeRef = new ParameterizedTypeReference<LinkedHashMap<String, Object>>() {
	};

	public LinkedHashMap<String, Object> process(HttpServletRequest request, String requestBody)
			throws NoAvailableInstanceException {
		HttpMethod httpMethod = HttpMethod.valueOf(request.getMethod());

		HttpHeaders headers = new HttpHeaders();
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			headers.set(headerName, request.getHeader(headerName));
		}

		HttpEntity<String> httpEntity = new HttpEntity<>(requestBody, headers);

		ApiServerInstance instance = roundRobinService.getApiServerInstance();

		ResponseEntity<LinkedHashMap<String, Object>> responseEntity = restTemplate
				.exchange(buildFullUrl(instance.getUrl(), request), httpMethod, httpEntity, responseBodyTypeRef);

		responseEntity.getBody().put("processedBy", instance.getUrl());

		afterServe(instance, responseEntity.getBody());

		return responseEntity.getBody();
	}

	@Async
	private void afterServe(ApiServerInstance instance, LinkedHashMap<String, Object> response) {
		reportService.getOrCreateInstanceReport(instance.getUrl()).increaseRequestCount();

		managementService.getInstanceList().forEach(inst -> {
			if (inst.getStatus() == InstanceStatus.DOWN) {
				return;
			}

			if (inst.getCoolDownPeriodStart() > 0
					&& inst.getCoolDownPeriodStart() + 1000 * coolDownPeriod > System.currentTimeMillis()) {
				return;
			}

			if (inst.getCoolDownPeriodStart() > 0
					&& inst.getCoolDownPeriodStart() + 1000 * coolDownPeriod < System.currentTimeMillis()) {
				inst.setCoolDownPeriodStart(0);
				inst.setAverageProcessTime(0);
			}

			if (inst.getAverageProcessTime() > maxProcessTime) {
				inst.setStatus(InstanceStatus.SLOW);
				inst.setCoolDownPeriodStart(System.currentTimeMillis());
			} else {
				inst.setStatus(InstanceStatus.OK);
			}
		});
	}

	private String buildFullUrl(String instanceUrl, HttpServletRequest request) {
		StringBuilder urlBuilder = new StringBuilder(instanceUrl);
		urlBuilder.append(request.getRequestURI());

		if (!request.getParameterMap().isEmpty()) {
			urlBuilder.append("?");
			for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
				String paramName = entry.getKey();
				List<String> paramValue = Arrays.asList(entry.getValue());
				urlBuilder.append(paramName).append("=").append(String.join(",", paramValue)).append("&");
			}
			urlBuilder.deleteCharAt(urlBuilder.length() - 1);
		}

		return urlBuilder.toString();
	}

	@Autowired
	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

}
