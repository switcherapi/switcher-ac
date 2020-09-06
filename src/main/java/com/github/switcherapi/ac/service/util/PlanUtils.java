package com.github.switcherapi.ac.service.util;

import java.beans.FeatureDescriptor;
import java.util.Arrays;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import com.github.switcherapi.ac.model.Plan;
import com.github.switcherapi.ac.model.PlanDTO;

public class PlanUtils {
	
	private PlanUtils() {}
	
	public static void loadAttributes(PlanDTO from, Plan to) {
		BeanUtils.copyProperties(from, to, getNullPropertyNames(from));
	}
	
	public static String[] getNullPropertyNames(Object source) {
	    final BeanWrapper src = new BeanWrapperImpl(source);
	    return Arrays.stream(src.getPropertyDescriptors())
	    	.map(FeatureDescriptor::getName)
	    	.filter(nullable -> src.getPropertyValue(nullable) == null)
	    	.toArray(String[]::new);
	}
}
