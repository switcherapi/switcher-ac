package com.github.switcherapi.ac.service;

import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.github.switcherapi.ac.model.response.GitHubDetailResponse;

@Service
public class GitHubService {
	
	private static final Logger logger = LogManager.getLogger(GitHubService.class);
	
	private static final String HEADER_ACCEPT = "accept";
	
	private static final String HEADER_JSON = "application/json";
	
	private static final String INVALID_ACCOUNT = "Invalid GitHub account";
	
	@Value("${service.github.clientid}")
	private String clientId;
	
	@Value("${service.github.secret}")
	private String oauthSecret;
	
	private Client client;
	
	private String gitUrlAccess;
	
	private String gitUrlDetail;
	
	public GitHubService(String tokenUrl, String detailUrl) {
		this.gitUrlAccess = tokenUrl;
		this.gitUrlDetail = detailUrl;
		this.client = ClientBuilder.newClient();
	}
	
	public GitHubService() {
		this(
			"https://github.com/login/oauth/access_token?client_id=%s&client_secret=%s&code=%s",
			"https://api.github.com/user"
		);
	}
	
	public String getToken(String code) {
		try {
			final WebTarget myResource = client.target(
					String.format(gitUrlAccess, clientId, oauthSecret, code));
			
			final Response response = myResource
				.request(MediaType.APPLICATION_JSON)
				.header(HEADER_ACCEPT, HEADER_JSON)
				.post(null);
			
			Map<?, ?> responseEntity = response.readEntity(Map.class);
			if (responseEntity.containsKey("access_token"))
				return responseEntity.get("access_token").toString();
			
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_ACCOUNT);
		} catch (Exception e) {
			logger.error("Failed to get token from GitHub - {}", e.getMessage());
			throw new ResponseStatusException(
					HttpStatus.UNAUTHORIZED, INVALID_ACCOUNT);
		}
	}
	
	public GitHubDetailResponse getGitHubDetail(String token) {
		try {
			final WebTarget myResource = client.target(gitUrlDetail);
			final Response response = myResource
				.request(MediaType.APPLICATION_JSON)
				.header(HEADER_ACCEPT, HEADER_JSON)
				.header("Authorization", String.format("token %s", token))
				.get();
			
			if (response.getStatus() == 200)
				return response.readEntity(GitHubDetailResponse.class);
			
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_ACCOUNT);
		} catch (Exception e) {
			logger.error("Failed to get user detailt - {}", e.getMessage());
			throw new ResponseStatusException(
					HttpStatus.UNAUTHORIZED, INVALID_ACCOUNT);
		}
	}

}
