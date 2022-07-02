package com.github.switcherapi.ac.service.validator;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

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

import com.github.switcherapi.ac.model.domain.FeaturePayload;
import com.github.switcherapi.ac.model.dto.ResponseRelayDTO;

@Component
public class ValidatorFactory {
	
	private static final Logger logger = LogManager.getLogger(ValidatorFactory.class);
	
	private static final String VALIDATORS_PATH = "com.github.switcherapi.ac.service.validator.beans";
	
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
    
    public ResponseRelayDTO runValidator(FeaturePayload request) {	
		if (validatorHandlers.containsKey(request.getFeature())) {
			final var validatorService = validatorHandlers.get(request.getFeature());
			return validatorService.execute(request);
		}
		
		throw new ResponseStatusException(
				HttpStatus.BAD_REQUEST, String.format("Invalid validator: %s", request.getFeature()));
 
    }

}
