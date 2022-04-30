package com.github.switcherapi.ac.model.mapper;

import com.github.switcherapi.ac.model.domain.Account;
import com.github.switcherapi.ac.model.dto.AccountDTO;
import com.github.switcherapi.ac.model.dto.PlanDTO;

public class AccountMapper {
	
	private AccountMapper() {}
	
	public static AccountDTO createCopy(Account from) {
		var to = DefaultMapper.createCopy(from, AccountDTO.class);
		to.setPlan(DefaultMapper.createCopy(from.getPlan(), PlanDTO.class));
		return to;
	}

}
