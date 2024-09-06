package com.github.switcherapi.ac.model.mapper;

import com.github.switcherapi.ac.model.domain.Account;
import com.github.switcherapi.ac.model.dto.AccountDTO;
import com.github.switcherapi.ac.model.dto.PlanDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AccountMapper {
	
	public static AccountDTO createCopy(Account from) {
		final var plan = new PlanDTO(from.getPlan().getId(), from.getPlan().getName(), from.getPlan().getAttributes());
		return new AccountDTO(from.getId(), from.getAdminId(), plan);
	}

}
