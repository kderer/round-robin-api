package com.coda.assignment.api.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.coda.assignment.api.dto.ApiRequestDto;
import com.coda.assignment.api.dto.ApiResponseDto;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ApiService {

	public ApiResponseDto process(ApiRequestDto requestDto) {
		String uuid = UUID.randomUUID().toString();

		log.info("Handled request with transaction id: " + uuid);

		return ApiResponseDto.builder().requestDto(requestDto).transactionId(uuid).build();
	}
}
