package com.github.switcherac.config;

import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Configuration
public class MongoDbConfiguration extends AbstractMongoClientConfiguration {
	
	@Value("${spring.data.mongodb.connection-string}")
	private String connectionString;
	
	@Value("${spring.data.mongodb.database}")
	private String databasePrefix;

	@Override
	protected String getDatabaseName() {
		return databasePrefix;
	}
	
	@Bean
    public MongoClient createMongoClient() throws UnknownHostException {
    	return MongoClients.create(connectionString);
    }
	
}