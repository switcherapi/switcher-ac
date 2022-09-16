package com.github.switcherapi.ac.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfiguration {
	
	private static final String SCHEME_NAME = "bearerScheme";
	
	private static final String SCHEME = "Bearer";

	private final ConfigProperties configProperties;

	public OpenAPIConfiguration(ConfigProperties configProperties) {
		this.configProperties = configProperties;
	}

	@Bean
	public OpenAPI customOpenAPI() {
		var openApi = new OpenAPI()
				.addServersItem(new Server().url(configProperties.getUrl()))
				.info(getInfo());

		addSecurity(openApi);
		return openApi;
	}

	private Info getInfo() {
		return new Info()
				.title(configProperties.getTitle())
				.description(configProperties.getDescription())
				.version(configProperties.getVersion())
				.contact(getContact())
				.license(getLicense());
	}

	private License getLicense() {
		return new License()
				.name(configProperties.getLicense().getType())
				.url(configProperties.getLicense().getUrl());
	}
	
	private Contact getContact() {
		return new Contact()
				.name(configProperties.getContact().getAuthor())
				.email(configProperties.getContact().getEmail());
	}

	private void addSecurity(OpenAPI openApi) {
		var components = createComponents();
		var securityItem = new SecurityRequirement().addList(SCHEME_NAME);

		openApi.components(components).addSecurityItem(securityItem);
	}

	private Components createComponents() {
		var components = new Components();
		components.addSecuritySchemes(SCHEME_NAME, createSecurityScheme());

		return components;
	}

	private SecurityScheme createSecurityScheme() {
		return new SecurityScheme()
				.name(SCHEME_NAME)
				.type(SecurityScheme.Type.HTTP)
				.scheme(SCHEME);
	}
    
}