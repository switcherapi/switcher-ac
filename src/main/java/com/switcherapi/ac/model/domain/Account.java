package com.github.switcherapi.ac.model.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@JsonInclude(Include.NON_NULL)
@Document(collection = "accounts")
@Data
@NoArgsConstructor
public class Account {

	@Id
	private String id;

	@Indexed(unique = true)
	private String adminId;

	private String plan;

	public Account(String adminId) {
		this.adminId = adminId;
	}

}
