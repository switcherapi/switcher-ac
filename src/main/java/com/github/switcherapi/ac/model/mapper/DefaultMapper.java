package com.github.switcherapi.ac.model.mapper;

import java.beans.FeatureDescriptor;
import java.util.Arrays;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import lombok.AccessLevel;
import lombok.Generated;
import lombok.NoArgsConstructor;

@Generated
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DefaultMapper {
	
	public static <T, Y> void copyProperties(T from, Y to) {
		BeanUtils.copyProperties(from, to, getNullPropertyNames(from));
	}
	
	public static <T, Y> Y createCopy(T from, Class<Y> clazz) {
		try {
			var to = clazz.getDeclaredConstructor().newInstance();
			BeanUtils.copyProperties(from, to, getNullPropertyNames(from));
			return to;
		} catch (Exception e) {
			return null;
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
