package com.github.switcherapi.ac.model.domain;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@JsonInclude(Include.NON_NULL)
@Document(collection = "accounts")
@Data
public class Account {

	@Id
	private String id;

	@Indexed(unique = true)
	private String adminId;

	@DBRef
	private Plan plan;

	private Date lastReset = new Date();

	private int currentDailyExecution;

}
