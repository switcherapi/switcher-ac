package com.github.switcherapi.ac.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "service.docs")
public record ConfigProperties(
    String title,
    String description,
    String version,
    String releaseTime,
    String url,
    License license,
    Contact contact) {

    public record License(
        String type,
        String url) { }

    public record Contact(
        String author,
        String email) { }
}
