package com.coda.assignment.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiRequestDto {
	private String game;
	private String gamerID;
	private int points;
}
