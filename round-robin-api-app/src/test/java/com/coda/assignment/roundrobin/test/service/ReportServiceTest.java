package com.coda.assignment.roundrobin.test.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.Vector;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import com.coda.assignment.roundrobin.enumaration.InstanceStatus;
import com.coda.assignment.roundrobin.exception.NoAvailableInstanceException;
import com.coda.assignment.roundrobin.service.ApiService;
import com.coda.assignment.roundrobin.service.ManagementService;
import com.coda.assignment.roundrobin.service.ReportService;
import com.coda.assignment.roundrobin.service.RoundRobinService;

import jakarta.servlet.http.HttpServletRequest;

@SpringBootTest
@ActiveProfiles("test")
public class ReportServiceTest {
	@Autowired
	private RoundRobinService roundRobinService;

	@Autowired
	private ManagementService managementService;

	@Autowired
	private ApiService apiService;

	@Autowired
	private ReportService reportService;

	ParameterizedTypeReference<LinkedHashMap<String, Object>> responseBodyTypeRef = new ParameterizedTypeReference<LinkedHashMap<String, Object>>() {
	};

	@BeforeEach
	private void initializeManagementService() {
		managementService.removeAllInstances();
		roundRobinService.resetInstanceCounter();
		reportService.removeAllReports();

		managementService.addInstance("http://localhost:8081");
		managementService.addInstance("http://localhost:8082");
		managementService.addInstance("http://localhost:8083");

		managementService.getInstanceList().forEach(instance -> {
			instance.setStatus(InstanceStatus.OK);
		});
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testReport() throws NoAvailableInstanceException {
		HttpServletRequest request = mock(HttpServletRequest.class);
		ResponseEntity<LinkedHashMap<String, Object>> responseEntity = new ResponseEntity<LinkedHashMap<String, Object>>(
				new LinkedHashMap<>(), HttpStatus.OK);
		RestTemplate restTemplate = mock(RestTemplate.class);

		when(request.getRequestURI()).thenReturn("/api");
		when(request.getMethod()).thenReturn("POST");
		when(request.getHeaderNames()).thenReturn((new Vector<String>()).elements());
		when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
				ArgumentMatchers.any(), ArgumentMatchers.any(ParameterizedTypeReference.class)))
				.thenReturn(responseEntity);

		apiService.setRestTemplate(restTemplate);

		// Report created for 8081
		apiService.process(request, null);
		assertEquals(responseEntity.getBody().get("processedBy"), "http://localhost:8081");
		// Report created for 8082
		apiService.process(request, null);
		assertEquals(responseEntity.getBody().get("processedBy"), "http://localhost:8082");
		// Report created for 8083
		apiService.process(request, null);
		assertEquals(responseEntity.getBody().get("processedBy"), "http://localhost:8083");

		apiService.process(request, null);
		assertEquals(responseEntity.getBody().get("processedBy"), "http://localhost:8081");
		apiService.process(request, null);
		assertEquals(responseEntity.getBody().get("processedBy"), "http://localhost:8082");

		assertEquals(reportService.getInstanceInstanceReportList().stream()
				.filter(report -> report.getUrl().equals("http://localhost:8081")).findAny().get()
				.getTotalRequestCount(), 2);
		assertEquals(reportService.getInstanceInstanceReportList().stream()
				.filter(report -> report.getUrl().equals("http://localhost:8082")).findAny().get()
				.getTotalRequestCount(), 2);

		assertEquals(reportService.getInstanceInstanceReportList().stream()
				.filter(report -> report.getUrl().equals("http://localhost:8083")).findAny().get()
				.getTotalRequestCount(), 1);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testReportForRemovedInstance() throws NoAvailableInstanceException {
		HttpServletRequest request = mock(HttpServletRequest.class);
		ResponseEntity<LinkedHashMap<String, Object>> responseEntity = new ResponseEntity<LinkedHashMap<String, Object>>(
				new LinkedHashMap<>(), HttpStatus.OK);
		RestTemplate restTemplate = mock(RestTemplate.class);

		when(request.getRequestURI()).thenReturn("/api");
		when(request.getMethod()).thenReturn("POST");
		when(request.getHeaderNames()).thenReturn((new Vector<String>()).elements());
		when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
				ArgumentMatchers.any(), ArgumentMatchers.any(ParameterizedTypeReference.class)))
				.thenReturn(responseEntity);

		apiService.setRestTemplate(restTemplate);

		// Report created for 8081
		apiService.process(request, null);
		assertEquals(responseEntity.getBody().get("processedBy"), "http://localhost:8081");
		// Report created for 8082
		apiService.process(request, null);
		assertEquals(responseEntity.getBody().get("processedBy"), "http://localhost:8082");
		// Report created for 8083
		apiService.process(request, null);
		assertEquals(responseEntity.getBody().get("processedBy"), "http://localhost:8083");

		managementService.removeInstance("http://localhost:8081");

		assertEquals("http://localhost:8083", roundRobinService.getApiServerInstance().getUrl());
		assertEquals("http://localhost:8082", roundRobinService.getApiServerInstance().getUrl());
		assertEquals("http://localhost:8083", roundRobinService.getApiServerInstance().getUrl());
		assertEquals("http://localhost:8082", roundRobinService.getApiServerInstance().getUrl());

		assertEquals(managementService.getInstanceList().size(), 2);
		assertNotEquals(
				reportService.getInstanceInstanceReportList().stream()
						.filter(report -> report.getUrl().equals("http://localhost:8081")).findAny().orElse(null),
				null);
	}

}
