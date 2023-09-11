package com.coda.assignment.api.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coda.assignment.api.dto.TestResponseDto;

@RestController
@RequestMapping("/test")
public class TestController {

	@GetMapping
	public TestResponseDto handleRequest() {
		return TestResponseDto.builder().uuid(UUID.randomUUID().toString()).build();
	}

}
