package com.github.switcherapi.ac.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
	
	public static final String AUTHORIZATION_HEADER = "Authorization";
	
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
        	.select()
            .apis(RequestHandlerSelectors.basePackage("com.github.switcherapi.ac.controller"))
            .paths(PathSelectors.any())
            .build()
            .apiInfo(apiInfo())
            .securityContexts(Arrays.asList(securityContext()))
            .securitySchemes(Arrays.asList(apiKey()))
            .useDefaultResponseMessages(false)
            .globalResponses(HttpMethod.GET, List.of(
        		new ResponseBuilder().code("500").description("Something went wrong").build(),
                new ResponseBuilder().code("403") .description("Unauthorized access").build()));
    }
    
    private ApiKey apiKey() { 
        return new ApiKey("JWT", AUTHORIZATION_HEADER, "header"); 
    }

    private ApiInfo apiInfo() {
    	Contact contact = new Contact(
    			"Roger Floriano (petruki)", 
    			"https://github.com/petruki", 
    			"switcher.project@gmail.com");
    	
        return new ApiInfo(
        		"Switcher AC", 
        		"Account Controller for Switcher API", 
        		"1.0.4", 
        		StringUtils.EMPTY, 
        		contact, 
        		"MIT", 
        		"https://github.com/switcherapi/switcher-ac/blob/master/LICENSE", 
        		Collections.emptyList());
    }
    
    private SecurityContext securityContext() { 
        return SecurityContext.builder().securityReferences(defaultAuth()).build(); 
    } 

    private List<SecurityReference> defaultAuth() { 
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything"); 
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1]; 
        authorizationScopes[0] = authorizationScope; 
        return List.of(new SecurityReference("JWT", authorizationScopes)); 
    }
    
}