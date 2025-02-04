package com.github.switcherapi.ac.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.github.switcherapi.ac.model.GitHubDetail;
import com.github.switcherapi.ac.service.facades.GitHubFacade;

import java.util.Objects;

@Service
public class GitHubService {

	private final GitHubFacade githubFacade;

	public GitHubService(GitHubFacade githubFacade) {
		this.githubFacade = githubFacade;
	}

	public String getToken(String code) {
		var token = githubFacade.getToken(code);

		if (!token.isEmpty()) {
			return token;
		}
		
		throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, GitHubFacade.INVALID_ACCOUNT);
	}
	
	public GitHubDetail getGitHubDetail(String token) {
		var response = githubFacade.getGitHubDetail(token);

		if (Objects.nonNull(response)) {
			return response;
		}
			
		throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, GitHubFacade.INVALID_ACCOUNT);
	}

}
