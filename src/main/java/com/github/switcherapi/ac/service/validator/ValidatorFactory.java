package com.github.switcherapi.ac.service.validator;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.github.switcherapi.ac.model.request.RequestRelay;
import com.github.switcherapi.ac.model.response.ResponseRelay;

@Component
public class ValidatorFactory {
	
	private static final Logger logger = LogManager.getLogger(ValidatorFactory.class);
	
	private static final String VALIDATORS_PATH = "com.github.switcherapi.ac.service.validator.beans";
	
	public static final String SEPARATOR = "#";
	
	@Autowired
	private AutowireCapableBeanFactory autowireCapableBeanFactory;
	
	private Map<String, AbstractValidatorService> validatorHandlers = new HashMap<>();
	
	@PostConstruct
	private void scanValidators() {
		final var provider = new ClassPathScanningCandidateComponentProvider(true);
		provider.addIncludeFilter(new AnnotationTypeFilter(SwitcherValidator.class, false, true));
		Set<BeanDefinition> beans = provider.findCandidateComponents(VALIDATORS_PATH);
		
		final List<String> filteredClasses = new ArrayList<>();
		beans.stream().forEach(beanDefinition -> filteredClasses.add(beanDefinition.getBeanClassName()));

        filteredClasses.stream().forEach(this::cacheValidator);
	}
	
    private void cacheValidator(String controllerClassName) {
        try {
            Class<?> validatorClass = Class.forName(controllerClassName);
            Annotation validatorAnnotation = validatorClass.getDeclaredAnnotation(SwitcherValidator.class);
            if (validatorAnnotation != null) {
            	SwitcherValidator sValidator = validatorClass.getAnnotation(SwitcherValidator.class);
            	
    			final AbstractValidatorService validatorService = 
    					(AbstractValidatorService) validatorClass.getConstructor().newInstance();

    			autowireCapableBeanFactory.autowireBean(validatorService);
    			validatorHandlers.put(sValidator.value(), validatorService);
            }
        } catch (ReflectiveOperationException e) {
        	logger.error("Failed to initialize validator - {}", e.getMessage());
        }
    }
    
    private String getValidatorName(RequestRelay request) {
    	if (request != null && request.getValue() != null)
    		return request.getValue().split(SEPARATOR)[0];
    	return StringUtils.EMPTY;
    }
    
    public ResponseRelay runValidator(RequestRelay request) {	
		final String validatorName = getValidatorName(request);
		if (validatorHandlers.containsKey(validatorName)) {
			final AbstractValidatorService validatorService = validatorHandlers.get(validatorName);
			return validatorService.execute(request);
		}
		
		throw new ResponseStatusException(
				HttpStatus.BAD_REQUEST, String.format("Invalid validator: %s", validatorName));
 
    }

}
