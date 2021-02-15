package com.github.switcherapi.ac.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.github.switcherapi.ac.model.response.GitHubDetailResponse;
import com.github.switcherapi.ac.service.facades.GitHubFacade;

@Service
public class GitHubService {
	
	@Autowired
	private GitHubFacade githubFacade;
	
	public String getToken(String code) {
		final String token = githubFacade.getToken(code);
		if (!token.isEmpty())
			return token;
		
		throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, GitHubFacade.INVALID_ACCOUNT);
	}
	
	public GitHubDetailResponse getGitHubDetail(String token) {
		final GitHubDetailResponse response = githubFacade.getGitHubDetail(token);
		if (response != null)
			return response;
			
		throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, GitHubFacade.INVALID_ACCOUNT);
	}
	
	public void setGithubFacade(GitHubFacade githubFacade) {
		this.githubFacade = githubFacade;
	}

}
