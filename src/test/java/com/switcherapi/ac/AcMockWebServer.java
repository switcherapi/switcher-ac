package com.switcherapi.ac;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.switcherapi.ac.model.GitHubDetail;
import lombok.extern.slf4j.Slf4j;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.net.HttpURLConnection;

@Slf4j
public class AcMockWebServer {

	private final ObjectMapper mapper = new ObjectMapper();

	protected static MockWebServer mockBackend;

	@BeforeAll
	static void setup() throws IOException {
		mockBackend = new MockWebServer();
		mockBackend.start();
	}

	@AfterAll
	static void tearDown() {
		mockBackend.close();
	}

	protected void givenResponse401() {
		mockBackend.enqueue(new MockResponse.Builder()
				.code(HttpURLConnection.HTTP_UNAUTHORIZED)
				.build());
	}

	protected void givenResponse503() {
		mockBackend.enqueue(new MockResponse.Builder()
				.code(HttpURLConnection.HTTP_UNAVAILABLE)
				.build());
	}

	protected void givenResponseInvalidCode() {
		mockBackend.enqueue(new MockResponse.Builder()
				.body("{ \"error\": \"Invalid code\" }")
				.build());
	}

	protected void givenResponseSuccess() throws JsonProcessingException {
		final var githubAccountDetail = new GitHubDetail("123", "UserName", "login", "http://avatar.com");

		mockBackend.enqueue(new MockResponse.Builder()
				.code(HttpURLConnection.HTTP_OK)
				.body(mapper.writeValueAsString(githubAccountDetail))
				.build());
	}

	protected void givenGitHubToken() {
		mockBackend.enqueue(new MockResponse.Builder()
				.code(HttpURLConnection.HTTP_OK)
				.body("{\"access_token\":\"123\",\"token_type\":\"bearer\",\"scope\":\"\"}")
				.build());
	}

	protected void givenGitHubDetails() {
		var githubAccountDetail = new GitHubDetail(
				"123", "UserName", "login", "http://avatar.com");

		try {
			mockBackend.enqueue(new MockResponse.Builder()
					.code(HttpURLConnection.HTTP_OK)
					.body(mapper.writeValueAsString(githubAccountDetail))
					.build());
		} catch (JsonProcessingException e) {
			log.error("Error on parsing GitHubDetail", e);
		}
	}

	protected void givenGitHubTokenInvalid() {
		mockBackend.enqueue(new MockResponse.Builder()
				.code(HttpURLConnection.HTTP_BAD_REQUEST)
				.build());
	}
}
