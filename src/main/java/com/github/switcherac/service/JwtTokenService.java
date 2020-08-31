package com.github.switcherac.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {
	
	@Value("${service.api.token}")
	private String token;
	
	public Boolean validateToken(String token) {
		return token.equals(token);
	}
}