package com.github.switcherapi.ac.model.mapper;

import com.github.switcherapi.ac.model.domain.Admin;
import com.github.switcherapi.ac.model.dto.AdminDTO;
import com.github.switcherapi.ac.model.dto.GitHubAuthDTO;

import lombok.AccessLevel;
import lombok.Generated;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

@Generated
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GitHubAuthMapper {
	
	public static GitHubAuthDTO createCopy(Admin from, Pair<String, String> tokens) {
		var to = new GitHubAuthDTO();
		to.setToken(tokens.getLeft());
		to.setRefreshToken(tokens.getRight());
		to.setAdmin(DefaultMapper.createCopy(from, AdminDTO.class));
		return to;
	}

}
