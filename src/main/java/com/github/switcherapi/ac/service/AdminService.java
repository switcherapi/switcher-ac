package com.github.switcherapi.ac.service;

import static com.github.switcherapi.ac.config.SwitcherFeatures.*;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.github.switcherapi.ac.config.JwtRequestFilter;
import com.github.switcherapi.ac.model.Admin;
import com.github.switcherapi.ac.model.response.GitHubDetailResponse;
import com.github.switcherapi.ac.repository.AdminRepository;

@Service
public class AdminService {
	
	public static final String ADMIN = "admin";
	
	public static final String TOKEN = "token";
	
	public static final String REFRESH_TOKEN = "refreshToken";
	
	@Autowired
	private AdminRepository adminRepository;
	
	@Autowired
	private GitHubService githubService;
	
	@Autowired
	private JwtTokenService jwtService;
	
	private boolean isAvailable(String githubId) {
		return getSwitcher(SWITCHER_AC_ADM)
				.checkValue(githubId)
				.isItOn();
	}
	
	public Map<String, Object> gitHubAuth(String code) {
		Map<String, Object> response = new HashMap<>();
		final String gitHubToken = githubService.getToken(code);
		final GitHubDetailResponse gitHubDetail = githubService.getGitHubDetail(gitHubToken);
		
		if (isAvailable(gitHubDetail.getId())) {
			Admin admin = adminRepository.findByGitHubId(gitHubDetail.getId());
			if (admin == null) {
				admin = createAdminAccount(gitHubDetail.getId());
			}
			
			final String[] tokens = jwtService.generateToken(admin.getId());
			updateAdminAccountToken(admin, tokens[0]);
			
			response.put(ADMIN, admin);
			response.put(TOKEN, tokens[0]);
			response.put(REFRESH_TOKEN, tokens[1]);
			
			return response;
		}
		
		throw new ResponseStatusException(
				HttpStatus.UNAUTHORIZED, "User not allowed");
	}
	
	public void logout(String token) {
		if (token != null && token.startsWith(JwtRequestFilter.BEARER)) {
			token = token.substring(7);
		}
		
		Admin admin = adminRepository.findByToken(token);
		if (admin != null)
			updateAdminAccountToken(admin, null);
	}
	
	public Admin createAdminAccount(String gitHubId) {
		Admin admin = adminRepository.findByGitHubId(gitHubId);
		
		if (admin == null)
			admin = new Admin();
		
		admin.setGitHubId(gitHubId);
		return adminRepository.save(admin);
	}
	
	public void updateAdminAccountToken(Admin admin, String token) {
		admin.setToken(token);
		adminRepository.save(admin);
	}
	
	public Map<String, Object> refreshToken(String token, String refreshToken) {
		if (token != null && token.startsWith(JwtRequestFilter.BEARER)) {
			token = token.substring(7);
			
			Map<String, Object> response = new HashMap<>();
			Admin admin = adminRepository.findByToken(token);
			
			if (admin != null) {
				String[] tokens = jwtService.refreshToken(admin.getId(), token, refreshToken);
				if (tokens.length == 2) {
					updateAdminAccountToken(admin, tokens[0]);
					
					response.put(TOKEN, tokens[0]);
					response.put(REFRESH_TOKEN, tokens[1]);
					return response;
				}
			}
		}
		
		throw new ResponseStatusException(
				HttpStatus.UNAUTHORIZED, "Invalid refresh tokens");
	}

	public void setGithubService(GitHubService githubService) {
		this.githubService = githubService;
	}

}
