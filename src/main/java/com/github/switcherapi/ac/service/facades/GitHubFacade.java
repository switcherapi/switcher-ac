package com.github.switcherapi.ac.service.facades;

import com.github.switcherapi.ac.model.GitHubDetail;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.github.switcherapi.ac.util.Sanitizer.*;

@Component
@Slf4j
public class GitHubFacade {
	
	public static final String INVALID_ACCOUNT = "Invalid GitHub account";
	
	private static final String ACCESS_TOKEN = "access_token";

	private final Gson gson = new Gson();

	private final String clientId;

	private final String oauthSecret;

	private final String gitUrlAccess;

	private final String gitUrlDetail;

	private final WebClient webClient;

	public GitHubFacade(
			@Value("${service.github.clientid}") String clientId,
			@Value("${service.github.secret}") String oauthSecret,
			@Value("${service.github.url.access}") String gitUrlAccess,
			@Value("${service.github.url.detail}") String gitUrlDetail) {
		this.clientId = clientId;
		this.oauthSecret = oauthSecret;
		this.gitUrlAccess = gitUrlAccess;
		this.gitUrlDetail = gitUrlDetail;
		this.webClient = WebClient.create();
	}

	public Mono<String> getToken(String code) {
		var codeSanitized = sanitize(code, List.of(trim(), alphaNumeric()));

		return webClient.post()
				.uri(String.format(gitUrlAccess, clientId, oauthSecret, codeSanitized))
				.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
				.retrieve()
				.bodyToMono(String.class)
				.map(response -> {
					if (!response.contains(ACCESS_TOKEN)) {
						log.error("Failed to get token from GitHub");
						return StringUtils.EMPTY;
					}
					var responseEntity = gson.fromJson(response, Map.class);
					return responseEntity.get(ACCESS_TOKEN).toString();
				})
				.onErrorMap(getThrowableThrowableFunction());
	}

	public Mono<GitHubDetail> getGitHubDetail(String token) {
		var tokenSanitized = sanitize(token, List.of(trim(), alphaNumeric("_")));

		return webClient.get()
				.uri(gitUrlDetail)
				.headers(headers -> {
					headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
					headers.set(HttpHeaders.AUTHORIZATION, String.format("token %s", tokenSanitized));
				})
				.retrieve()
				.bodyToMono(String.class)
				.map(response -> gson.fromJson(response, GitHubDetail.class))
				.onErrorMap(getThrowableThrowableFunction());
	}

	private Function<? super Throwable, ? extends Throwable> getThrowableThrowableFunction() {
		return e -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_ACCOUNT, e);
	}

}
