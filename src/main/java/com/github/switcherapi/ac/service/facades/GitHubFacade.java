package com.github.switcherapi.ac.service.facades;

import com.github.switcherapi.ac.model.GitHubDetail;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Component
public class GitHubFacade {
	
	private static final Logger logger = LogManager.getLogger(GitHubFacade.class);
	
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
		final WebTarget myResource = client.target(
				String.format(gitUrlAccess, clientId, oauthSecret, code));

		try (var response = myResource
				.request(MediaType.APPLICATION_JSON)
				.header(HEADER_ACCEPT, MediaType.APPLICATION_JSON)
				.post(null)) {

			if (response.getStatus() == 200) {
				final var responseEntity = response.readEntity(Map.class);
				if (responseEntity.containsKey(ACCESS_TOKEN))
					return responseEntity.get(ACCESS_TOKEN).toString();
			}
			
			return StringUtils.EMPTY;
		} catch (Exception e) {
			logger.error("Failed to get token from GitHub - {}", e.getMessage());
			throw new ResponseStatusException(
					HttpStatus.UNAUTHORIZED, INVALID_ACCOUNT);
		}
	}
	
	public GitHubDetail getGitHubDetail(String token) {
		final WebTarget myResource = client.target(gitUrlDetail);

		try (var response = myResource
			.request(MediaType.APPLICATION_JSON)
			.header(HEADER_ACCEPT, MediaType.APPLICATION_JSON)
			.header(HttpHeaders.AUTHORIZATION, String.format("token %s", token))
			.get()) {
		
			if (response.getStatus() == 200)
				return response.readEntity(GitHubDetail.class);

			return null;
		} catch (Exception e) {
			logger.error("Failed to get GitHub detail - {}", e.getMessage());
			throw new ResponseStatusException(
					HttpStatus.UNAUTHORIZED, INVALID_ACCOUNT);
		}
	}

}
