package com.nector.catalogservice.scheduler;

import java.time.LocalDate;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.nector.catalogservice.service.CompanyTaxCategoryService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CompanyTaxCategoryScheduler {

	private final CompanyTaxCategoryService companyTaxCategoryService;
	
	@Scheduled(cron = "0 0 13 * * ?")
	public void deactivateExpiredTaxCategories() {
		 companyTaxCategoryService.deactivateExpiredTaxCategories(LocalDate.now());
	}
}
