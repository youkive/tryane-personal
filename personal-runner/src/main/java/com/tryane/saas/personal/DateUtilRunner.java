package com.tryane.saas.personal;

import org.joda.time.LocalDate;
import org.joda.time.Weeks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateUtilRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(DateUtilRunner.class);

	public static void main(String[] args) {
		Integer nbWeeks = Weeks.weeksBetween(LocalDate.parse("2016-01-01"), LocalDate.now()).getWeeks();
		LOGGER.info("nb Weeks : {}", nbWeeks);
	}
}
