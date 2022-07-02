package com.github.switcherapi.ac.service;

import static com.github.switcherapi.ac.config.SwitcherFeatures.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.github.switcherapi.ac.config.JwtRequestFilter;
import com.github.switcherapi.ac.model.domain.Admin;
import com.github.switcherapi.ac.model.dto.GitHubAuthDTO;
import com.github.switcherapi.ac.model.mapper.GitHubAuthMapper;
import com.github.switcherapi.ac.repository.AdminRepository;

@Service
public class AdminService {
	
	private static final Logger logger = LogManager.getLogger(AdminService.class);
	
	public static final String ADMIN = "admin";
	
	public static final String TOKEN = "token";
	
	public static final String REFRESH_TOKEN = "refreshToken";
	
	private final AdminRepository adminRepository;
	
	private final JwtTokenService jwtService;
	
	private GitHubService githubService;
	
	public AdminService(AdminRepository adminRepository, 
			GitHubService githubService, JwtTokenService jwtService) {
		this.adminRepository = adminRepository;
		this.githubService = githubService;
		this.jwtService = jwtService;
	}

	private boolean isAvailable(String githubId) {
		return getSwitcher(SWITCHER_AC_ADM)
				.checkValue(githubId)
				.isItOn();
	}
	
	public GitHubAuthDTO gitHubAuth(String code) {
		final var gitHubToken = githubService.getToken(code);
		final var gitHubDetail = githubService.getGitHubDetail(gitHubToken);
		
		if (isAvailable(gitHubDetail.getId())) {
			var admin = adminRepository.findByGitHubId(gitHubDetail.getId());
			if (admin == null) {
				admin = createAdminAccount(gitHubDetail.getId());
			}
			
			final String[] tokens = jwtService.generateToken(admin.getId());
			updateAdminAccountToken(admin, tokens[0]);
			
			return GitHubAuthMapper.createCopy(admin, tokens);
		}
		
		throw new ResponseStatusException(
				HttpStatus.UNAUTHORIZED, "User not allowed");
	}
	
	public void logout(String token) {
		if (token != null && token.startsWith(JwtRequestFilter.BEARER)) {
			token = token.substring(7);
		}
		
		var admin = adminRepository.findByToken(token);
		if (admin != null)
			updateAdminAccountToken(admin, null);
	}
	
	public Admin createAdminAccount(String gitHubId) {
		var admin = adminRepository.findByGitHubId(gitHubId);
		
		if (admin == null)
			admin = new Admin();
		
		admin.setGitHubId(gitHubId);
		return adminRepository.save(admin);
	}
	
	public void updateAdminAccountToken(Admin admin, String token) {
		admin.setToken(token);
		adminRepository.save(admin);
	}
	
	public GitHubAuthDTO refreshToken(String token, String refreshToken) {
		try {
			if (token != null && token.startsWith(JwtRequestFilter.BEARER)) {
				token = token.substring(7);
				
				var admin = adminRepository.findByToken(token);
				if (admin != null) {
					String[] tokens = jwtService.refreshToken(admin.getId(), token, refreshToken);
					if (tokens.length == 2) {
						updateAdminAccountToken(admin, tokens[0]);
						return GitHubAuthMapper.createCopy(admin, tokens);
					}
				}
			}
		} catch (Exception e) {
			logger.info("Attempting to refresh token with Invalid refresh tokens");
		}
		
		throw new ResponseStatusException(
				HttpStatus.UNAUTHORIZED, "Invalid refresh tokens");

	}

	public void setGithubService(GitHubService githubService) {
		this.githubService = githubService;
	}

}
