package com.coda.assignment.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coda.assignment.api.dto.ApiRequestDto;
import com.coda.assignment.api.dto.ApiResponseDto;
import com.coda.assignment.api.service.ApiService;

@RestController
@RequestMapping("/api")
public class ApiController {
	@Autowired
	private ApiService apiService;

	@PostMapping
	public ApiResponseDto handlePost(@RequestBody ApiRequestDto requestDto,
			@RequestParam(required = false) boolean exception) {

		if (exception) {
			throw new RuntimeException();
		}

		return apiService.process(requestDto);
	}

}
