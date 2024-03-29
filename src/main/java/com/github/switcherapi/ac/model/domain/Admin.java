package com.github.switcherapi.ac.model.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@JsonInclude(Include.NON_NULL)
@Document(collection = "admins")
@Data
public class Admin {

	@Id
	private String id;

	@JsonIgnore
	private String token;

	@Indexed(unique = true)
	private String gitHubId;

}
