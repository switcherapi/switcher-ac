package com.github.switcherapi.ac.model.mapper;

import com.github.switcherapi.ac.model.domain.Admin;
import com.github.switcherapi.ac.model.dto.AdminDTO;
import com.github.switcherapi.ac.model.dto.GitHubAuthDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GitHubAuthMapper {

	public static GitHubAuthDTO createCopy(Admin from, Pair<String, String> tokens) {
		final var admin = new AdminDTO(from.getId(), from.getGitHubId());
		return new GitHubAuthDTO(admin, tokens.getLeft(), tokens.getRight());
	}

}
