package com.github.switcherapi.ac.service.validator;

import com.github.switcherapi.ac.model.domain.FeaturePayload;
import com.github.switcherapi.ac.model.dto.ResponseRelayDTO;
import com.github.switcherapi.ac.repository.AccountDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.lang.annotation.Annotation;
import java.util.*;

@Component
public class ValidatorFactory {
	
	private static final Logger logger = LogManager.getLogger(ValidatorFactory.class);
	
	private static final String VALIDATORS_PATH = "com.github.switcherapi.ac.service.validator.beans";

	private final AutowireCapableBeanFactory autowireCapableBeanFactory;

	private final AccountDao accountDao;
	
	private final Map<String, AbstractValidatorService> validatorHandlers = new HashMap<>();

	public ValidatorFactory(AutowireCapableBeanFactory autowireCapableBeanFactory, AccountDao accountDao) {
		this.autowireCapableBeanFactory = autowireCapableBeanFactory;
		this.accountDao = accountDao;
		this.scanValidators();
	}

	private void scanValidators() {
		final var provider = new ClassPathScanningCandidateComponentProvider(true);
		provider.addIncludeFilter(new AnnotationTypeFilter(SwitcherValidator.class, false, true));
		Set<BeanDefinition> beans = provider.findCandidateComponents(VALIDATORS_PATH);
		
		final List<String> filteredClasses = new ArrayList<>();
		beans.forEach(beanDefinition -> filteredClasses.add(beanDefinition.getBeanClassName()));

        filteredClasses.forEach(this::cacheValidator);
	}
	
    private void cacheValidator(String controllerClassName) {
        try {
            Class<?> validatorClass = Class.forName(controllerClassName);
            Annotation validatorAnnotation = validatorClass.getDeclaredAnnotation(SwitcherValidator.class);
            if (validatorAnnotation != null) {
            	SwitcherValidator sValidator = validatorClass.getAnnotation(SwitcherValidator.class);
            	
    			final AbstractValidatorService validatorService = 
    					(AbstractValidatorService) validatorClass.getConstructor(AccountDao.class).newInstance(this.accountDao);

    			autowireCapableBeanFactory.autowireBean(validatorService);
    			validatorHandlers.put(sValidator.value(), validatorService);
            }
        } catch (ReflectiveOperationException e) {
        	logger.error("Failed to initialize validator - {}", e.getMessage());
        }
    }
    
    public ResponseRelayDTO runValidator(FeaturePayload request) {	
		if (!validatorHandlers.containsKey(request.getFeature())) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST, String.format("Invalid validator: %s", request.getFeature()));
		}

		final var validatorService = validatorHandlers.get(request.getFeature());
		return validatorService.execute(request);
    }

}
