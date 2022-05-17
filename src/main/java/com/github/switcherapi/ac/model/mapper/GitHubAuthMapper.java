package com.github.switcherapi.ac.model.mapper;

import com.github.switcherapi.ac.model.domain.Admin;
import com.github.switcherapi.ac.model.dto.AdminDTO;
import com.github.switcherapi.ac.model.dto.GitHubAuthDTO;

import lombok.AccessLevel;
import lombok.Generated;
import lombok.NoArgsConstructor;

@Generated
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GitHubAuthMapper {
	
	public static GitHubAuthDTO createCopy(Admin from, String[] tokens) {
		var to = new GitHubAuthDTO();
		to.setToken(tokens[0]);
		to.setRefreshToken(tokens[1]);
		to.setAdmin(DefaultMapper.createCopy(from, AdminDTO.class));
		return to;
	}

}
