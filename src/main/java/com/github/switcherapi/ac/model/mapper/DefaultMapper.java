package com.github.switcherapi.ac.model.mapper;

import lombok.AccessLevel;
import lombok.Generated;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.FeatureDescriptor;
import java.util.ArrayList;
import java.util.Arrays;

@Generated
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DefaultMapper {
	
	public static <T, Y> void copyProperties(T from, Y to, String... ignoreProperties) {
		var ignored = new ArrayList<>(Arrays.asList(getNullPropertyNames(from)));
		ignored.addAll(Arrays.asList(ignoreProperties));

		BeanUtils.copyProperties(from, to, ignored.toArray(String[]::new));
	}

	public static <T, Y> Y createCopy(T from, Y to) {
		BeanUtils.copyProperties(from, to, getNullPropertyNames(from));
		return to;
	}
	
	public static String[] getNullPropertyNames(Object source) {
	    final BeanWrapper src = new BeanWrapperImpl(source);
	    return Arrays.stream(src.getPropertyDescriptors())
	    	.map(FeatureDescriptor::getName)
	    	.filter(nullable -> src.getPropertyValue(nullable) == null)
	    	.toArray(String[]::new);
	}

}
