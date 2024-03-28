package com.github.switcherapi.ac.service.validator;

import com.github.switcherapi.ac.model.domain.FeaturePayload;
import com.github.switcherapi.ac.model.dto.ResponseRelayDTO;
import com.github.switcherapi.ac.repository.AccountDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class ValidatorFactory {
	
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
		var beans = provider.findCandidateComponents(VALIDATORS_PATH);

		var filteredClasses = new ArrayList<String>();
		beans.forEach(beanDefinition -> filteredClasses.add(beanDefinition.getBeanClassName()));

        filteredClasses.forEach(this::cacheValidator);
	}
	
    private void cacheValidator(String controllerClassName) {
        try {
            var validatorClass = Class.forName(controllerClassName);
            var validatorAnnotation = validatorClass.getDeclaredAnnotation(SwitcherValidator.class);
            if (validatorAnnotation != null) {
            	var sValidator = validatorClass.getAnnotation(SwitcherValidator.class);
            	var validatorService = (AbstractValidatorService) validatorClass.getConstructor(AccountDao.class).newInstance(this.accountDao);

    			autowireCapableBeanFactory.autowireBean(validatorService);
    			validatorHandlers.put(sValidator.value(), validatorService);
            }
        } catch (ReflectiveOperationException e) {
        	log.error("Failed to initialize validator - {}", e.getMessage());
        }
    }
    
    public ResponseRelayDTO runValidator(FeaturePayload request) {	
		if (!validatorHandlers.containsKey(request.feature())) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST, String.format("Invalid validator: %s", request.feature()));
		}

		final var validatorService = validatorHandlers.get(request.feature());
		return validatorService.execute(request);
    }

}
