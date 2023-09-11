package com.coda.assignment.roundrobin.controller;

import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.coda.assignment.roundrobin.exception.NoAvailableInstanceException;
import com.coda.assignment.roundrobin.service.ApiService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("{_:^(?!swagger-ui|error).*$}*")
public class ApiController {
	@Autowired
	private ApiService apiService;

	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE })
	public LinkedHashMap<String, Object> handleRequest(HttpServletRequest request,
			@RequestBody(required = false) String requestBody) throws NoAvailableInstanceException {
		return apiService.process(request, requestBody);
	}

}
