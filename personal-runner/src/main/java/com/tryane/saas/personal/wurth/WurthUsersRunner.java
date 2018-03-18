package com.tryane.saas.personal.wurth;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.tryane.saas.personal.config.PersonalAppConfig;
import com.tryane.saas.personal.wurth.item.TryaneExportUser;
import com.tryane.saas.personal.wurth.item.WurthADUser;
import com.tryane.saas.personal.wurth.reader.TryaneExportReader;
import com.tryane.saas.personal.wurth.reader.TryaneExportReaderCallback;
import com.tryane.saas.personal.wurth.reader.WurthADReader;
import com.tryane.saas.personal.wurth.reader.WurthADReaderCallback;

public class WurthUsersRunner {

	public static final String	TRYANE_EXPORT_INTEGRATION	= "com/tryane/saas/personal/wurth/listemembres_tryane_export_integration.csv";

	public static final String	TRYANE_EXPORT_PRODUCTION	= "com/tryane/saas/personal/wurth/listemembres_tryane_export_production.csv";

	public static final String	EXPORT_AD					= "com/tryane/saas/personal/wurth/wurth_export_AD.csv";

	private static final String	REPORT						= "src/main/resources/com/tryane/saas/personal/wurth/report.log";

	private static final Logger	LOGGER						= LoggerFactory.getLogger(WurthUsersRunner.class);

	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx = null;
		try {
			ctx = new AnnotationConfigApplicationContext(PersonalAppConfig.class);

			WurthUsersRunner runner = new WurthUsersRunner();
			ctx.getAutowireCapableBeanFactory().autowireBean(runner);

			runner.execute();
		} finally {
			if (ctx != null) {
				ctx.close();
			}
		}
	}

	public void execute() {
		Set<WurthADUser> adUsers = readAdUserExport();
		readTryaneExportIntegration();
		TryaneUsersGroup tryaneProductionUsersGroup = readTryaneExportProduction();

		try {
			analyseProduction(adUsers, tryaneProductionUsersGroup);
		} catch (IOException e) {
			LOGGER.error("", e);
		}
	}

	public void analyseProduction(Set<WurthADUser> adItem, TryaneUsersGroup tryaneProductionUsersGroup) throws IOException {
		Set<String> ADUsersIds = adItem.stream().map(item -> item.getId().toLowerCase()).collect(Collectors.toSet());
		Set<String> tryaneProductionUserIds = tryaneProductionUsersGroup.getTryaneUsers().stream().map(item -> item.getId()).collect(Collectors.toSet());

		Set<TryaneExportUser> usersTryaneProductionNotFoundInAD = new HashSet<>();
		AtomicLong nbMatchTryaneInAD = new AtomicLong(0L);
		tryaneProductionUsersGroup.getTryaneUsers().stream().forEach(item -> {
			if (!ADUsersIds.contains(item.getId())) {
				usersTryaneProductionNotFoundInAD.add(item);
			} else {
				nbMatchTryaneInAD.incrementAndGet();
			}
		});
		LOGGER.info("number tryane item not found in AD : {}", usersTryaneProductionNotFoundInAD.size());

		Set<WurthADUser> adItemNotFoundInTryane = new HashSet<>();
		AtomicLong nbMatchUsersADInTryane = new AtomicLong(0L);
		adItem.stream().forEach(item -> {
			if (!tryaneProductionUserIds.contains(item.getId().toLowerCase())) {
				adItemNotFoundInTryane.add(item);
			} else {
				nbMatchUsersADInTryane.incrementAndGet();
			}
		});

		LOGGER.info("Number ad item not found in tryane : {}", adItemNotFoundInTryane.size());

		File reportFile = new File(REPORT);
		if (!reportFile.exists()) {
			reportFile.createNewFile();
		}
		try (BufferedWriter buffer = Files.newBufferedWriter(reportFile.toPath(), StandardOpenOption.TRUNCATE_EXISTING)) {
			buffer.write("Users Tryane in doublon \n\n\n");
			buffer.write(tryaneProductionUsersGroup.getAdminTryaneUsers().size() + " users\n\n");
			tryaneProductionUsersGroup.getAdminTryaneUsers().stream().forEach(user -> {
				try {
					buffer.write(user.toPrint());
					buffer.write("\n");
				} catch (IOException e) {
					LOGGER.error("", e);
				}
			});

			buffer.write("\n\n\nUser Tryane not found in AD \n\n\n");
			buffer.write(usersTryaneProductionNotFoundInAD.size() + " users not found\n");
			buffer.write(nbMatchTryaneInAD.get() + " users matched\n\n");
			usersTryaneProductionNotFoundInAD.stream().forEach(user -> {
				try {
					buffer.write(user.toPrint());
					buffer.write("\n");
				} catch (IOException e) {
					LOGGER.error("", e);
				}
			});
			buffer.flush();

			buffer.write("\n\n\nAD Users not found in tryane\n\n\n");
			buffer.write(adItemNotFoundInTryane.size() + " users not found \n");
			buffer.write(nbMatchUsersADInTryane.get() + " users matched\n\n");
			adItemNotFoundInTryane.stream().forEach(user -> {
				try {
					buffer.write(user.toPrint());
					buffer.write("\n");
				} catch (IOException e) {
					LOGGER.error("", e);
				}
			});
			buffer.flush();
		} catch (IOException e) {
			LOGGER.error("", e);
		}
	}

	public Set<WurthADUser> readAdUserExport() {
		Resource adUsersResource = new PathMatchingResourcePatternResolver().getResource(EXPORT_AD);
		try (WurthADReader reader = new WurthADReader(adUsersResource.getFile())) {
			WurthADReaderCallback callback = new WurthADReaderCallback();
			reader.readReport(callback);
			return callback.getUsersIdentifiersAD();
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		return null;
	}

	public TryaneUsersGroup readTryaneExportIntegration() {
		return readTryaneExport(TRYANE_EXPORT_INTEGRATION);
	}

	public TryaneUsersGroup readTryaneExportProduction() {
		return readTryaneExport(TRYANE_EXPORT_PRODUCTION);
	}

	public TryaneUsersGroup readTryaneExport(String exportResourcePath) {
		Resource tryaneExportIntegrationResource = new PathMatchingResourcePatternResolver().getResource(exportResourcePath);
		try (TryaneExportReader reader = new TryaneExportReader(tryaneExportIntegrationResource.getFile())) {
			TryaneExportReaderCallback callback = new TryaneExportReaderCallback();
			reader.readReport(callback);
			callback.finish();
			return new TryaneUsersGroup(callback.getUsersInTryane(), callback.getAdminInTryane());
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		return null;
	}

	class TryaneUsersGroup {
		private Set<TryaneExportUser>	tryaneUsers;

		private Set<TryaneExportUser>	adminTryaneUsers;

		public TryaneUsersGroup(Set<TryaneExportUser> tryaneUsers, Set<TryaneExportUser> adminTryaneUsers) {
			this.tryaneUsers = tryaneUsers;
			this.adminTryaneUsers = adminTryaneUsers;
		}

		public Set<TryaneExportUser> getTryaneUsers() {
			return tryaneUsers;
		}

		public Set<TryaneExportUser> getAdminTryaneUsers() {
			return adminTryaneUsers;
		}

	}
}
