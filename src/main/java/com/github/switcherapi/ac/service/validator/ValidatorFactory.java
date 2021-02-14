package com.github.switcherapi.ac.service.validator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import com.github.switcherapi.ac.model.response.ResponseRelay;

@Component
public class ValidatorFactory {
	
	private static final Logger logger = LogManager.getLogger(ValidatorFactory.class);
	
	private final String VALIDATORS_PATH = "com.github.switcherapi.ac.service.validator.beans";
	
	@Autowired
	private AutowireCapableBeanFactory autowireCapableBeanFactory;
	
	private Map<String, Class<?>> validators = new HashMap<>();
	
	public void scanValidators() {
		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(true);
		provider.addIncludeFilter(new AnnotationTypeFilter(SwitcherValidator.class, false, true));
		Set<BeanDefinition> beans = provider.findCandidateComponents(VALIDATORS_PATH);
		
		final List<String> filteredClasses = new ArrayList<>();
		beans.stream().forEach(beanDefinition -> filteredClasses.add(beanDefinition.getBeanClassName()));

        filteredClasses.stream().forEach(filteredClass -> storeValidator(filteredClass));
	}
	
    private void storeValidator(String controllerClassName) {
        try {
            Class<?> clazz = Class.forName(controllerClassName);
            Annotation validatorAnnotation = clazz.getDeclaredAnnotation(SwitcherValidator.class);
            if (validatorAnnotation != null) {
            	SwitcherValidator sValidator = (SwitcherValidator) clazz.getAnnotation(SwitcherValidator.class);
            	validators.put(sValidator.value(), clazz);
            }
        } catch (ClassNotFoundException e) {
        	logger.error("Failed to initialize validator - {}", e.getMessage());
        }
    }
    
    public ResponseRelay runValidator(String name, Object... args) throws Exception {
    	if (validators.containsKey(name)) {
        	Class<?> clazz = validators.get(name);
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(SwitcherCheck.class)) {
                    method.setAccessible(true);
                    
                    Object obj = clazz.getDeclaredConstructor().newInstance();
                    autowireCapableBeanFactory.autowireBean(obj);
                    return (ResponseRelay) method.invoke(obj, args);
                }
            }
    	}
    	
		throw new ResponseStatusException(
				HttpStatus.BAD_REQUEST, String.format("Invalid validator: %s", name));
    }

}
