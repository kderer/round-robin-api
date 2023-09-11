package com.coda.assignment.roundrobin.dto;

import lombok.Data;

@Data
public class ApiRequestDto {
	private String game;
	private String gamerID;
	private int points;

}
