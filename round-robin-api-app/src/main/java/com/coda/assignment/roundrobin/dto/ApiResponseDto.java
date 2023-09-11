package com.coda.assignment.roundrobin.dto;

import lombok.Data;

@Data
public class ApiResponseDto {
	private ApiRequestDto requestDto;
	private String transactionId;
	private String processedBy;
	private int processDuration;
}
