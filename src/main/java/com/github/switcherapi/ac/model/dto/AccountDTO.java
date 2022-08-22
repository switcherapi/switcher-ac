package com.github.switcherapi.ac.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

import java.util.Date;

@JsonInclude(Include.NON_NULL)
@Data
public class AccountDTO {

	private String id;

	private String adminId;

	private PlanDTO plan;

	private Date lastReset;

	private int currentDailyExecution;

}
