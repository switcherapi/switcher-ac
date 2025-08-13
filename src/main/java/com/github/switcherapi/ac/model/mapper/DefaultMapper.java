package com.github.switcherapi.ac.model.mapper;

import com.switcherapi.client.exception.SwitcherException;
import lombok.experimental.UtilityClass;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.FeatureDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;

@UtilityClass
public class DefaultMapper {
	
	public static <T, Y> void copyProperties(T from, Y to, String... ignoreProperties) {
		var ignored = new ArrayList<>(Arrays.asList(getNullPropertyNames(from)));
		ignored.addAll(Arrays.asList(ignoreProperties));

		BeanUtils.copyProperties(from, to, ignored.toArray(String[]::new));
	}

	public static <T, Y> Y createCopy(T from, Class<Y> type) {
		try {
			var to = type.getConstructor().newInstance();
			BeanUtils.copyProperties(from, to, getNullPropertyNames(from));
			return to;
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new SwitcherException(String.format("Failed to create a copy of %s", from), e);
		}
	}
	
	public static String[] getNullPropertyNames(Object source) {
	    final BeanWrapper src = new BeanWrapperImpl(source);
	    return Arrays.stream(src.getPropertyDescriptors())
	    	.map(FeatureDescriptor::getName)
	    	.filter(nullable -> src.getPropertyValue(nullable) == null)
	    	.toArray(String[]::new);
	}

}
