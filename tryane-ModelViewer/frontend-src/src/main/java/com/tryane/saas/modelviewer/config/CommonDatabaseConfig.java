package com.tryane.saas.modelviewer.config;

import java.sql.Driver;
import java.util.Properties;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

@PropertySource("classpath:common.datasource.properties")
@Configuration
@EnableJpaRepositories(basePackages = "com.tryane.saas.modelviewer.dao.common", entityManagerFactoryRef = "commonEntityManagerFactoryBean", transactionManagerRef = "commonTransactionManager")
public class CommonDatabaseConfig {

	private static final Logger	LOGGER						= LoggerFactory.getLogger(CommonDatabaseConfig.class);

	public static final String	DATABASE_COMMONCLIENT_NAME	= "database.commonclient.name";

	public static final String	JDBC_DRIVER_CLASS_NAME		= "jdbc.driverClassName";
	public static final String	JDBC_PASSWORD				= "jdbc.password";
	public static final String	JDBC_USERNAME				= "jdbc.username";
	public static final String	JDBC_URL					= "jdbc.url";

	@Autowired
	private Environment			env;

	@SuppressWarnings("unchecked")
	@Bean
	public DataSource dataSource() {
		SimpleDriverDataSource datasource = new SimpleDriverDataSource();
		try {
			datasource.setDriverClass((Class<Driver>) Class.forName(env.getProperty(JDBC_DRIVER_CLASS_NAME)));
		} catch (ClassNotFoundException e) {
			LOGGER.error("", e);
			return null;
		}
		datasource.setUrl(env.getProperty(JDBC_URL) + env.getProperty(DATABASE_COMMONCLIENT_NAME));
		datasource.setUsername(env.getProperty(JDBC_USERNAME));
		datasource.setPassword(env.getProperty(JDBC_PASSWORD));

		Properties props = new Properties();
		props.put("autocommit", false);
		datasource.setConnectionProperties(props);
		return datasource;
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean commonEntityManagerFactoryBean() {
		LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
		em.setDataSource(dataSource());
		em.setPackagesToScan("com.tryane.saas");
		em.setPersistenceUnitName("saas-client-data");

		JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		em.setJpaVendorAdapter(vendorAdapter);
		em.setJpaProperties(additionalProperties());

		return em;
	}

	@Bean
	public PlatformTransactionManager commonTransactionManager() {
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(commonEntityManagerFactoryBean().getObject());
		return transactionManager;
	}

	Properties additionalProperties() {
		Properties properties = new Properties();
		properties.setProperty("hibernate.jdbc.batch_size", env.getProperty("hibernate.jdbc.batch_size", "50"));
		properties.setProperty("hibernate.dialect", env.getProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQL9Dialect"));
		properties.setProperty("hibernate.show_sql", env.getProperty("hibernate.show_sql", "false"));
		properties.setProperty("hibernate.use_sql_comments", env.getProperty("hibernate.use_sql_comments", "false"));
		properties.setProperty("hibernate.connection.autocommit", env.getProperty("hibernate.connection.autocommit", "false"));
		String sqlScript = env.getProperty("import.sql");
		if (sqlScript != null) {
			properties.setProperty("hibernate.hbm2ddl.import_files", sqlScript);
		}
		return properties;
	}

}
