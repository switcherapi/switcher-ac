package com.github.switcherapi.ac.service.validator;

import com.github.switcherapi.ac.repository.AccountDao;
import com.github.switcherapi.ac.repository.PlanDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Objects;

@Slf4j
@Component
@ConditionalOnProperty(value = "service.validators.native", havingValue = "false")
public class ValidatorRuntimeBuilder extends ValidatorBuilderService {
	
	private static final String VALIDATORS_PATH = "com.github.switcherapi.ac.service.validator.beans";

	private final AutowireCapableBeanFactory autowireCapableBeanFactory;

	public ValidatorRuntimeBuilder(AutowireCapableBeanFactory autowireCapableBeanFactory, AccountDao accountDao, PlanDao planDao) {
		super(accountDao, planDao);
		this.autowireCapableBeanFactory = autowireCapableBeanFactory;
		this.initializeValidators();
	}

	@Override
	protected void initializeValidators() {
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

            if (Objects.nonNull(validatorAnnotation)) {
            	var sValidator = validatorClass.getAnnotation(SwitcherValidator.class);
            	var validatorService = (AbstractValidatorService) validatorClass
						.getConstructor(AccountDao.class, PlanDao.class)
						.newInstance(this.accountDao, this.planDao);

    			autowireCapableBeanFactory.autowireBean(validatorService);
    			validatorHandlers.put(sValidator.value(), validatorService);
            }
        } catch (ReflectiveOperationException e) {
        	log.error("Failed to initialize validator - {}", e.getMessage());
        }
    }

}
