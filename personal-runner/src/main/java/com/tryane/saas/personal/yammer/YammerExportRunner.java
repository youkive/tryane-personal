package com.tryane.saas.personal.yammer;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.tryane.saas.connector.yammer.api.export.YammerExportModel;
import com.tryane.saas.connector.yammer.common.model.export.YammerExportFile;
import com.tryane.saas.connector.yammer.newapi.ICachedYammerExportAPI;
import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.utils.exception.TryaneAuthenticationException;
import com.tryane.saas.utils.exception.TryaneConnectionException;
import com.tryane.saas.utils.exception.TryaneHttpErrorException;
import com.tryane.saas.utils.files.TemporaryFile;

public class YammerExportRunner {
	private static final Logger LOGGER = LoggerFactory.getLogger(YammerExportRunner.class);

	public static void main(String[] args) throws TryaneHttpErrorException, TryaneConnectionException, TryaneAuthenticationException {
		System.setProperty("spring.profiles.active", "dev");

		try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(PersonalAppConfig.class)) {
			ICachedYammerExportAPI exportApi = ctx.getAutowireCapableBeanFactory().getBean(ICachedYammerExportAPI.class);

			YammerExportFile exportFile = YammerExportFile.exportBetween(LocalDate.parse("2019-03-18"), LocalDate.parse("2019-03-25"), YammerExportModel.FILE);
			TemporaryFile exportSince = exportApi
					.getExport("22322-gMfZMwR9nc94KRjg8gNbg", YammerExportFile.exportBetween(LocalDate.parse("2019-03-18"), LocalDate.parse("2019-03-25"), YammerExportModel.MESSAGE));

			LOGGER.info("File downloaded to {}, please delete it", exportSince);

		}

	}
}
