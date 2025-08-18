package com.switcherapi.ac.config;

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

	private final ServiceConfig.Docs docs;

	public OpenAPIConfiguration(ServiceConfig serviceConfig) {
		this.docs = serviceConfig.docs();
	}

	@Bean
	public OpenAPI customOpenAPI() {
		var openApi = new OpenAPI()
				.addServersItem(new Server().url(docs.url()))
				.info(getInfo());

		addSecurity(openApi);
		return openApi;
	}

	private Info getInfo() {
		return new Info()
				.title(docs.title())
				.description(docs.description())
				.version(docs.version())
				.contact(getContact())
				.license(getLicense());
	}

	private License getLicense() {
		return new License()
				.name(docs.license().type())
				.url(docs.license().url());
	}
	
	private Contact getContact() {
		return new Contact()
				.name(docs.contact().author())
				.email(docs.contact().email());
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