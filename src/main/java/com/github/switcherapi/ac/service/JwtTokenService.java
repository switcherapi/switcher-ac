package com.github.switcherapi.ac.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {
	
	@Value("${service.api.token}")
	private String apiToken;
	
	@Value("${service.relay.token}")
	private String relayToken;
	
	public boolean validateAdminToken(String token) {
		return apiToken.equals(token);
	}
	
	public boolean validateRelayToken(String token) {
		return relayToken.equals(token);
	}
}