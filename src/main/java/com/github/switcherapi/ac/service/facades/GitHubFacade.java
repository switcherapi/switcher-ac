package com.github.switcherapi.ac.service.facades;

import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.github.switcherapi.ac.model.response.GitHubDetailResponse;

@Component
public class GitHubFacade {
	
	private static final Logger logger = LogManager.getLogger(GitHubFacade.class);
	
	public static final String INVALID_ACCOUNT = "Invalid GitHub account";
	
	private static final String ACCESS_TOKEN = "access_token";
	
	private static final String HEADER_ACCEPT = "accept";
	
	private static final String HEADER_JSON = "application/json";
	
	@Value("${service.github.clientid}")
	private String clientId;
	
	@Value("${service.github.secret}")
	private String oauthSecret;
	
	private String gitUrlAccess;
	
	private String gitUrlDetail;
	
	private Client client;
	
	public GitHubFacade() {
		this(
			"https://github.com/login/oauth/access_token?client_id=%s&client_secret=%s&code=%s",
			"https://api.github.com/user"
		);
	}
	
	public GitHubFacade(String tokenUrl, String detailUrl) {
		this.gitUrlAccess = tokenUrl;
		this.gitUrlDetail = detailUrl;
		this.client = ClientBuilder.newClient();
	}
	
	public String getToken(String code) {
		try {
			final WebTarget myResource = client.target(
					String.format(gitUrlAccess, clientId, oauthSecret, code));
			
			final var response = myResource
				.request(MediaType.APPLICATION_JSON)
				.header(HEADER_ACCEPT, HEADER_JSON)
				.post(null);
			
			Map<?, ?> responseEntity = response.readEntity(Map.class);
			if (responseEntity.containsKey(ACCESS_TOKEN))
				return responseEntity.get(ACCESS_TOKEN).toString();
			
			return StringUtils.EMPTY;
		} catch (Exception e) {
			logger.error("Failed to get token from GitHub - {}", e.getMessage());
			throw new ResponseStatusException(
					HttpStatus.UNAUTHORIZED, INVALID_ACCOUNT);
		}
	}
	
	public GitHubDetailResponse getGitHubDetail(String token) {
		final WebTarget myResource = client.target(gitUrlDetail);
		final var response = myResource
			.request(MediaType.APPLICATION_JSON)
			.header(HEADER_ACCEPT, HEADER_JSON)
			.header("Authorization", String.format("token %s", token))
			.get();
		
		if (response.getStatus() == 200)
			return response.readEntity(GitHubDetailResponse.class);
		return null;
	}

}
