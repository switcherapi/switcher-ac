package com.github.switcherapi.ac.model.mapper;

import com.github.switcherapi.ac.model.domain.Account;
import com.github.switcherapi.ac.model.dto.AccountDTO;
import com.github.switcherapi.ac.model.dto.PlanV2DTO;
import lombok.AccessLevel;
import lombok.Generated;
import lombok.NoArgsConstructor;

@Generated
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AccountMapper {
	
	public static AccountDTO createCopy(Account from) {
		var to = DefaultMapper.createCopy(from, new AccountDTO());
		to.setPlan(DefaultMapper.createCopy(from.getPlanV2(), new PlanV2DTO()));
		return to;
	}

}
