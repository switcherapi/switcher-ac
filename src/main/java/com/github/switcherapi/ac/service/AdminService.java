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
import reactor.core.publisher.Mono;

import java.util.Objects;

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

	public Mono<GitHubAuthDTO> gitHubAuth(String code) {
		final var gitHubToken = githubService.getToken(code);
		final var gitHubDetail = githubService.getGitHubDetail(gitHubToken);

		if (isNotAvailable(gitHubDetail.id())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not allowed");
		}

		return adminRepository.findByGitHubId(gitHubDetail.id())
				.switchIfEmpty(Mono.defer(() -> {
					var admin = new Admin();
					admin.setGitHubId(gitHubDetail.id());
					return adminRepository.save(admin);
				}))
				.flatMap(admin -> {
					final var tokens = jwtService.generateToken(admin.getId());
					updateAdminAccountToken(admin, tokens.getLeft());
					return Mono.just(GitHubAuthMapper.createCopy(admin, tokens));
				});
	}

	public Mono<Admin> logout(String token) {
		if (Objects.nonNull(token) && token.startsWith(JwtRequestFilter.BEARER)) {
			token = token.substring(7);
		}

		return adminRepository.findByToken(token)
				.flatMap(admin -> updateAdminAccountToken(admin, null));
	}

	public Mono<Admin> createAdminAccount(String gitHubId) {
		return adminRepository.findByGitHubId(gitHubId)
				.switchIfEmpty(Mono.just(new Admin()))
				.flatMap(admin -> {
					admin.setGitHubId(gitHubId);
					return adminRepository.save(admin);
				});
	}

	public Mono<Admin> updateAdminAccountToken(Admin admin, String token) {
		admin.setToken(token);
		return adminRepository.save(admin);
	}

	public Mono<GitHubAuthDTO> refreshToken(String token, String refreshToken) {
		if (Objects.isNull(token) || !token.startsWith(JwtRequestFilter.BEARER)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh tokens");
		}

		final var finalToken = token.substring(7);

		return adminRepository.findByToken(finalToken)
				.switchIfEmpty(Mono.just(new Admin()))
				.flatMap(admin -> updateRefreshToken(refreshToken, admin, finalToken));
	}

	private Mono<GitHubAuthDTO> updateRefreshToken(String refreshToken, Admin admin, String finalToken) {
		try {
			if (isNotAvailable(admin.getGitHubId())) {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not allowed");
			}

			var tokens = jwtService.refreshToken(admin.getId(), finalToken, refreshToken);
			if (Objects.nonNull(tokens)) {
				return updateAdminAccountToken(admin, tokens.getLeft())
						.map(updatedAdmin -> GitHubAuthMapper.createCopy(updatedAdmin, tokens));
			}
		} catch (Exception e) {
			log.warn("Attempting to refresh token with Invalid refresh tokens");
		}

		throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh tokens");
	}


}
