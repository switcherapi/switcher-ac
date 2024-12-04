package com.github.switcherapi.ac.service.facades;

import com.github.switcherapi.ac.model.GitHubDetail;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.github.switcherapi.ac.util.Sanitizer.*;

@Component
@Slf4j
public class GitHubFacade {
	
	public static final String INVALID_ACCOUNT = "Invalid GitHub account";
	
	private static final String ACCESS_TOKEN = "access_token";
	
	private static final String HEADER_ACCEPT = "accept";

	private final String clientId;

	private final String oauthSecret;

	private final String gitUrlAccess;

	private final String gitUrlDetail;
	
	private final Client client;

	public GitHubFacade(
			@Value("${service.github.clientid}") String clientId,
			@Value("${service.github.secret}") String oauthSecret,
			@Value("${service.github.url.access}") String gitUrlAccess,
			@Value("${service.github.url.detail}") String gitUrlDetail) {
		this.clientId = clientId;
		this.oauthSecret = oauthSecret;
		this.gitUrlAccess = gitUrlAccess;
		this.gitUrlDetail = gitUrlDetail;
		this.client = ClientBuilder.newClient();
	}
	
	public String getToken(String code) {
		var codeSanitized = sanitize(code, List.of(trim(), alphaNumeric()));

		final WebTarget myResource = client.target(
				String.format(gitUrlAccess, clientId, oauthSecret, codeSanitized));

		try (var response = myResource
				.request(MediaType.APPLICATION_JSON)
				.header(HEADER_ACCEPT, MediaType.APPLICATION_JSON)
				.post(null)) {

			if (response.getStatus() == 200) {
				final var responseEntity = response.readEntity(Map.class);
				if (responseEntity.containsKey(ACCESS_TOKEN)) {
					return responseEntity.get(ACCESS_TOKEN).toString();
				}
			}

			log.error("Failed to get token from GitHub");
			return StringUtils.EMPTY;
		}
	}
	
	public GitHubDetail getGitHubDetail(String token) {
		final WebTarget myResource = client.target(gitUrlDetail);

		try (var response = myResource
			.request(MediaType.APPLICATION_JSON)
			.header(HEADER_ACCEPT, MediaType.APPLICATION_JSON)
			.header(HttpHeaders.AUTHORIZATION, String.format("token %s", token))
			.get()) {
		
			if (response.getStatus() == 200) {
				return response.readEntity(GitHubDetail.class);
			}

			log.error("Failed to get GitHub detail");
			return null;
		}
	}

}
