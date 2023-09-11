package com.coda.assignment.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiResponseDto {
	private ApiRequestDto requestDto;
	private String transactionId;
}
