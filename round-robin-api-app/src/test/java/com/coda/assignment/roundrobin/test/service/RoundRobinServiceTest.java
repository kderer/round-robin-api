package com.coda.assignment.roundrobin.test.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import com.coda.assignment.roundrobin.service.RoundRobinService;

import jakarta.servlet.http.HttpServletRequest;

@SpringBootTest
@ActiveProfiles("test")
public class RoundRobinServiceTest {
	@Autowired
	private RoundRobinService roundRobinService;

	@Autowired
	private ManagementService managementService;

	@Autowired
	private ApiService apiService;

	ParameterizedTypeReference<LinkedHashMap<String, Object>> responseBodyTypeRef = new ParameterizedTypeReference<LinkedHashMap<String, Object>>() {
	};

	@BeforeEach
	private void initializeManagementService() {
		managementService.removeAllInstances();
		roundRobinService.resetInstanceCounter();

		managementService.addInstance("http://localhost:8081");
		managementService.addInstance("http://localhost:8082");
		managementService.addInstance("http://localhost:8083");

		managementService.getInstanceList().forEach(instance -> {
			instance.setStatus(InstanceStatus.OK);
		});
	}

	@Test
	public void testRoundRobinWithInitialStatus() throws NoAvailableInstanceException {
		assertEquals("http://localhost:8081", roundRobinService.getApiServerInstance().getUrl());
		assertEquals("http://localhost:8082", roundRobinService.getApiServerInstance().getUrl());
		assertEquals("http://localhost:8083", roundRobinService.getApiServerInstance().getUrl());
	}

	@Test
	public void testRoundRobinWithDown() throws NoAvailableInstanceException {
		managementService.getInstance("http://localhost:8082").setStatus(InstanceStatus.DOWN);

		assertEquals("http://localhost:8081", roundRobinService.getApiServerInstance().getUrl());
		assertEquals("http://localhost:8083", roundRobinService.getApiServerInstance().getUrl());
		assertEquals("http://localhost:8081", roundRobinService.getApiServerInstance().getUrl());
		assertEquals("http://localhost:8083", roundRobinService.getApiServerInstance().getUrl());
	}

	@Test
	public void testRoundRobinWithSlow() throws NoAvailableInstanceException {
		managementService.getInstance("http://localhost:8081").setStatus(InstanceStatus.SLOW);

		assertEquals("http://localhost:8082", roundRobinService.getApiServerInstance().getUrl());
		assertEquals("http://localhost:8083", roundRobinService.getApiServerInstance().getUrl());
		assertEquals("http://localhost:8082", roundRobinService.getApiServerInstance().getUrl());
		assertEquals("http://localhost:8083", roundRobinService.getApiServerInstance().getUrl());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testRoundRobinWithAverageProcessTime() throws Exception {
		managementService.getInstance("http://localhost:8081").setAverageProcessTime(750);

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

		apiService.process(request, null);

		assertEquals(responseEntity.getBody().get("processedBy"), "http://localhost:8081");

		assertEquals("http://localhost:8082", roundRobinService.getApiServerInstance().getUrl());
		assertEquals("http://localhost:8083", roundRobinService.getApiServerInstance().getUrl());
		assertEquals("http://localhost:8082", roundRobinService.getApiServerInstance().getUrl());
		assertEquals("http://localhost:8083", roundRobinService.getApiServerInstance().getUrl());

		Thread.sleep(10000);

		apiService.process(request, null);

		assertEquals(managementService.getInstance("http://localhost:8081").getStatus(), InstanceStatus.SLOW);

		Thread.sleep(25000);

		apiService.process(request, null);

		assertEquals(managementService.getInstance("http://localhost:8081").getStatus(), InstanceStatus.OK);

		assertEquals("http://localhost:8081", roundRobinService.getApiServerInstance().getUrl());
		assertEquals("http://localhost:8082", roundRobinService.getApiServerInstance().getUrl());
		assertEquals("http://localhost:8083", roundRobinService.getApiServerInstance().getUrl());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testRoundRobinLessSlow() throws Exception {
		managementService.getInstance("http://localhost:8081").setAverageProcessTime(750);
		managementService.getInstance("http://localhost:8082").setAverageProcessTime(650);
		managementService.getInstance("http://localhost:8083").setStatus(InstanceStatus.DOWN);

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

		// Processed by 8081 and it is marked SLOW
		apiService.process(request, null);

		// Picked 8082, because 8083 down and 8082 less slow
		assertEquals("http://localhost:8082", roundRobinService.getApiServerInstance().getUrl());

		// Picked 8081, because 8083 is down and next available is 8081
		assertEquals("http://localhost:8081", roundRobinService.getApiServerInstance().getUrl());

		managementService.getInstance("http://localhost:8081").setAverageProcessTime(550);

		// 8081's turn, and 8081 is less slow
		apiService.process(request, null);
		assertEquals(responseEntity.getBody().get("processedBy"), "http://localhost:8081");

		// 8082's turn but 8081's less slow
		assertEquals("http://localhost:8081", roundRobinService.getApiServerInstance().getUrl());

		// 8083's turn, next available is 8081
		assertEquals("http://localhost:8081", roundRobinService.getApiServerInstance().getUrl());

		// 8081's turn, 8081 is less slow
		assertEquals("http://localhost:8081", roundRobinService.getApiServerInstance().getUrl());

		// 8082's turn, but, 8081 is less slow
		assertEquals("http://localhost:8081", roundRobinService.getApiServerInstance().getUrl());
	}

	@Test
	public void testRemoveInstance() throws NoAvailableInstanceException {
		managementService.removeInstance("http://localhost:8081");

		assertEquals(managementService.getInstanceList().size(), 2);
		assertEquals("http://localhost:8082", roundRobinService.getApiServerInstance().getUrl());
		assertEquals("http://localhost:8083", roundRobinService.getApiServerInstance().getUrl());
		assertEquals("http://localhost:8082", roundRobinService.getApiServerInstance().getUrl());
		assertEquals("http://localhost:8083", roundRobinService.getApiServerInstance().getUrl());
	}

}
