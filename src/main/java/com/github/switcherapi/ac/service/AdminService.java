package com.github.switcherapi.ac.service;

import com.github.switcherapi.ac.config.JwtRequestFilter;
import com.github.switcherapi.ac.model.domain.Admin;
import com.github.switcherapi.ac.model.dto.GitHubAuthDTO;
import com.github.switcherapi.ac.model.mapper.GitHubAuthMapper;
import com.github.switcherapi.ac.repository.AdminRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static com.github.switcherapi.ac.config.SwitcherFeatures.SWITCHER_AC_ADM;
import static com.github.switcherapi.ac.config.SwitcherFeatures.getSwitcher;

@Service
@Slf4j
public class AdminService {
	
	private final AdminRepository adminRepository;
	
	private final JwtTokenService jwtService;
	
	private final GitHubService githubService;
	
	public AdminService(AdminRepository adminRepository,
						GitHubService githubService, JwtTokenService jwtService) {
		this.adminRepository = adminRepository;
		this.githubService = githubService;
		this.jwtService = jwtService;
	}

	private boolean isNotAvailable(String githubId) {
		return !getSwitcher(SWITCHER_AC_ADM)
				.checkValue(githubId)
				.isItOn();
	}
	
	public GitHubAuthDTO gitHubAuth(String code) {
		final var gitHubToken = githubService.getToken(code);
		final var gitHubDetail = githubService.getGitHubDetail(gitHubToken);
		
		if (isNotAvailable(gitHubDetail.getId())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not allowed");
		}

		var admin = adminRepository.findByGitHubId(gitHubDetail.getId());
		if (admin == null) {
			admin = createAdminAccount(gitHubDetail.getId());
		}

		final var tokens = jwtService.generateToken(admin.getId());
		updateAdminAccountToken(admin, tokens.getLeft());

		return GitHubAuthMapper.createCopy(admin, tokens);

	}
	
	public void logout(String token) {
		if (token != null && token.startsWith(JwtRequestFilter.BEARER)) {
			token = token.substring(7);
		}
		
		var admin = adminRepository.findByToken(token);
		if (admin != null) {
			updateAdminAccountToken(admin, null);
		}
	}
	
	public Admin createAdminAccount(String gitHubId) {
		var admin = adminRepository.findByGitHubId(gitHubId);
		
		if (admin == null) {
			admin = new Admin();
		}
		
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
					if (isNotAvailable(admin.getGitHubId())) {
						throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not allowed");
					}

					final var tokens = jwtService.refreshToken(admin.getId(), token, refreshToken);
					if (tokens != null) {
						updateAdminAccountToken(admin, tokens.getLeft());
						return GitHubAuthMapper.createCopy(admin, tokens);
					}
				}
			}
		} catch (Exception e) {
			log.warn("Attempting to refresh token with Invalid refresh tokens");
		}
		
		throw new ResponseStatusException(
				HttpStatus.UNAUTHORIZED, "Invalid refresh tokens");
	}

}
