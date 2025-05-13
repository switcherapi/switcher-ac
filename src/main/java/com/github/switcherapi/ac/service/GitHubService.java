package com.github.switcherapi.ac.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.github.switcherapi.ac.model.GitHubDetail;
import com.github.switcherapi.ac.service.facades.GitHubFacade;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
public class GitHubService {

	private final GitHubFacade githubFacade;

	public GitHubService(GitHubFacade githubFacade) {
		this.githubFacade = githubFacade;
	}

	public Mono<String> getToken(String code) {
		return githubFacade.getToken(code)
				.flatMap(token -> {
					if (token.isEmpty()) {
						return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, GitHubFacade.INVALID_ACCOUNT));
					}
					return Mono.just(token);
				});
	}
	
	public Mono<GitHubDetail> getGitHubDetail(String token) {
		return githubFacade.getGitHubDetail(token)
				.flatMap(gitHubDetail -> {
					if (Objects.isNull(gitHubDetail)) {
						return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, GitHubFacade.INVALID_ACCOUNT));
					}
					return Mono.just(gitHubDetail);
				});
	}

}
