package com.github.switcherapi.ac.service;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.switcherapi.client.SwitcherFactory;
import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.model.Entry;
import com.github.switcherapi.client.utils.SwitcherContextParam;

@Service
public class SwitcherService {
	
	private static final Logger logger = LogManager.getLogger(SwitcherService.class);
	
	@Value("${switcher.url}")
	private String switcherUrl;
	
	@Value("${switcher.apikey}")
	private String switcherKey;
	
	@Value("${switcher.environment}")
	private String switcherEnvironment;
	
	@Value("${switcher.domain}")
	private String switcherDomain;
	
	private Map<String, Object> properties;
	
	@PostConstruct
	public void buildContext() throws SwitcherException {
		properties = new HashMap<String, Object>();
		properties.put(SwitcherContextParam.URL, switcherUrl);
		properties.put(SwitcherContextParam.APIKEY, switcherKey);
		properties.put(SwitcherContextParam.DOMAIN, switcherDomain);
		properties.put(SwitcherContextParam.COMPONENT, "switcher-ac");
		properties.put(SwitcherContextParam.ENVIRONMENT, switcherEnvironment);
		
		SwitcherFactory.buildContext(properties, false);
	}
	
	public boolean isAvailable(String key, String githubId) {
		try {
			return SwitcherFactory
					.getSwitcher(key)
					.prepareEntry(new Entry(Entry.VALUE, githubId))
					.isItOn();
		} catch (SwitcherException e) {
			logger.error(e.getMessage());
		}
		
		return false;
	}

}
