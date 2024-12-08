package com.github.switcherapi.ac.service.facades;

import com.github.switcherapi.ac.model.GitHubDetail;
import com.github.switcherapi.client.exception.SwitcherRemoteException;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

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
	
	private final HttpClient httpClient;

	public GitHubFacade(
			@Value("${service.github.clientid}") String clientId,
			@Value("${service.github.secret}") String oauthSecret,
			@Value("${service.github.url.access}") String gitUrlAccess,
			@Value("${service.github.url.detail}") String gitUrlDetail) {
		this.clientId = clientId;
		this.oauthSecret = oauthSecret;
		this.gitUrlAccess = gitUrlAccess;
		this.gitUrlDetail = gitUrlDetail;
		this.httpClient = HttpClient.newHttpClient();
	}
	
	public String getToken(String code) {
		var codeSanitized = sanitize(code, List.of(trim(), alphaNumeric()));

		try {
			final URI uri = new URI(String.format(gitUrlAccess, clientId, oauthSecret, codeSanitized));

			final HttpResponse<String> response = httpClient.send(HttpRequest.newBuilder()
					.uri(uri)
					.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON.toString())
					.POST(HttpRequest.BodyPublishers.noBody())
					.build(), HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() != 200 || !response.body().contains(ACCESS_TOKEN)) {
				log.error("Failed to get token from GitHub");
				return StringUtils.EMPTY;
			}

			final var responseEntity = gson.fromJson(response.body(), Map.class);
			return responseEntity.get(ACCESS_TOKEN).toString();
		} catch (Exception e) {
			return exceptionHandler(e, gitUrlAccess);
		}
	}
	
	public GitHubDetail getGitHubDetail(String token) {
		try {
			final URI uri = new URI(gitUrlDetail);

			final HttpResponse<String> response = httpClient.send(HttpRequest.newBuilder()
					.uri(uri)
					.headers(
							HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON.toString(),
							HttpHeaders.AUTHORIZATION, String.format("token %s", token))
					.GET().build(), HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() != 200) {
				log.error("Failed to get GitHub detail");
				return null;
			}

			return gson.fromJson(response.body(), GitHubDetail.class);
		} catch (Exception e) {
			return exceptionHandler(e, gitUrlDetail);
		}
	}

	private <T> T exceptionHandler(Exception e, String url) {
		if (e instanceof InterruptedException) {
			Thread.currentThread().interrupt();
		}
		throw new SwitcherRemoteException(url, e);
	}

}
