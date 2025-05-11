package com.github.switcherapi.ac.model.mapper;

import com.github.switcherapi.ac.model.domain.Account;
import com.github.switcherapi.ac.model.domain.Plan;
import com.github.switcherapi.ac.model.dto.AccountDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AccountMapper {

	public static AccountDTO map(Account account, Plan plan) {
		return new AccountDTO(account.getId(), account.getAdminId(), PlanMapper.createCopy(plan));
	}

}
