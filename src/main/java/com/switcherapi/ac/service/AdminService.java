package com.switcherapi.ac.service;

import com.switcherapi.ac.model.domain.Admin;
import com.switcherapi.ac.model.dto.GitHubAuthDTO;
import com.switcherapi.ac.model.mapper.GitHubAuthMapper;
import com.switcherapi.ac.repository.AdminRepository;
import com.switcherapi.ac.service.security.JwtTokenService;
import com.switcherapi.ac.util.Roles;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

import static com.switcherapi.ac.config.SwitcherFeatures.SWITCHER_AC_ADM;
import static com.switcherapi.ac.config.SwitcherFeatures.getSwitcher;
import static com.switcherapi.ac.util.Constants.BEARER;

@Slf4j
@Service
public class AdminService {

	private final AdminRepository adminRepository;

	private final JwtTokenService jwtTokenService;

	private final ReactiveAuthenticationManager authenticationManager;

	private final GitHubService githubService;

	public AdminService(AdminRepository adminRepository,
						GitHubService githubService,
						JwtTokenService jwtTokenService,
						ReactiveAuthenticationManager authenticationManager) {
		this.adminRepository = adminRepository;
		this.githubService = githubService;
		this.jwtTokenService = jwtTokenService;
		this.authenticationManager = authenticationManager;
	}

	private boolean isNotAvailable(String githubId) {
		return !getSwitcher(SWITCHER_AC_ADM)
				.checkValue(githubId)
				.isItOn();
	}

	public Mono<GitHubAuthDTO> gitHubAuth(String code) {
		return getGitHubDetailId(code)
				.flatMap(gitHubDetailId -> adminRepository.findByGitHubId(gitHubDetailId)
						.switchIfEmpty(Mono.defer(() -> {
							var admin = new Admin();
							admin.setGitHubId(gitHubDetailId);
							return adminRepository.save(admin);
						}))
						.flatMap(admin -> authenticationManager.authenticate(
								new UsernamePasswordAuthenticationToken(
										admin.getId(), admin.getGitHubId(),
										List.of(new SimpleGrantedAuthority(Roles.ROLE_ADMIN.name()))))
								.flatMap(authentication -> {
										final var tokens = jwtTokenService.generateToken(authentication);
										return updateAdminAccountToken(admin, tokens.getLeft())
												.flatMap(updatedAdmin -> Mono.just(GitHubAuthMapper.createCopy(updatedAdmin, tokens)));
								})));
	}

	private Mono<String> getGitHubDetailId(String code) {
		return githubService.getToken(code)
				.flatMap(githubService::getGitHubDetail)
				.filter(gitHubDetail1 -> !isNotAvailable(gitHubDetail1.id()))
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not allowed")))
				.flatMap(gitHubDetail -> Mono.just(gitHubDetail.id()));
	}

	public Mono<Admin> logout(String token) {
		if (Objects.nonNull(token) && token.startsWith(BEARER.getValue())) {
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
		final var finalToken = token.substring(7);

		return adminRepository.findByToken(finalToken)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh tokens")))
				.flatMap(admin -> updateRefreshToken(refreshToken, admin, finalToken));
	}

	private Mono<GitHubAuthDTO> updateRefreshToken(String refreshToken, Admin admin, String finalToken) {
		try {
			if (isNotAvailable(admin.getGitHubId())) {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not allowed");
			}

			return authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(
							admin.getId(),
							admin.getGitHubId(),
							List.of(new SimpleGrantedAuthority(Roles.ROLE_ADMIN.name()))))
					.flatMap(authentication -> {
						var tokens = jwtTokenService.refreshToken(authentication, finalToken, refreshToken);
						return updateAdminAccountToken(admin, tokens.getLeft())
								.map(updatedAdmin -> GitHubAuthMapper.createCopy(updatedAdmin, tokens));
					})
					.onErrorResume(e -> {
						log.debug("Refresh token could not be processed for {}", admin.getGitHubId());
						return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh tokens"));
					});
		} catch (Exception e) {
			log.warn("Attempting to refresh token with Invalid refresh tokens", e);
		}

		throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh tokens");
	}

}
