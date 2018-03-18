package com.tryane.saas.personal.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.tryane.saas.core.config.db.AbstractSaasRoutingDataSource;
import com.tryane.saas.core.config.db.RoutingDataSourceFactory;

@Configuration
@EnableTransactionManagement
public class PersonalDatabaseConfig {

	@Autowired
	Environment env;

	private BasicDataSource createDataSource() {
		BasicDataSource datasource = new BasicDataSource();
		datasource.setDriverClassName(env.getProperty("jdbc.driverClassName"));
		datasource.setUsername(env.getProperty("jdbc.username"));
		datasource.setPassword(env.getProperty("jdbc.password"));
		return datasource;
	}

	@Bean
	public DataSource commonClientDataSource() {
		BasicDataSource datasource = createDataSource();
		datasource.setUrl(env.getProperty("jdbc.url") + env.getProperty("database.commonclient.name"));
		//SAAS-361: Config : pool de connection. Suppression des connexions apres un delais d'idle
		datasource.setMaxTotal(Integer.valueOf(env.getProperty("database.commonclient.maxConnections")));
		datasource.setMinIdle(Integer.valueOf(env.getProperty("database.commonclient.minidle")));
		datasource.setMinEvictableIdleTimeMillis(Integer.valueOf(env.getProperty("database.common.eviction.maxtime")));
		datasource.setTimeBetweenEvictionRunsMillis(Integer.valueOf(env.getProperty("database.common.eviction.between")));
		return datasource;
	}

	@Bean
	public DataSource routingDataSource() {
		AbstractSaasRoutingDataSource routingDataSource = RoutingDataSourceFactory.createRoutingDataSource(env);
		routingDataSource.setPrimaryDataSource(commonClientDataSource());

		routingDataSource.setDriverClassName(env.getProperty("jdbc.driverClassName"));
		routingDataSource.setUsername(env.getProperty("jdbc.username"));
		routingDataSource.setPassword(env.getProperty("jdbc.password"));
		routingDataSource.setDataBaseNamePrefix(env.getProperty("database.client.name.prefix"));
		routingDataSource.setUrlOptions(env.getProperty("jdbc.url.options"));
		routingDataSource.setDefaultHostUrl(env.getProperty("jdbc.url"));

		//SAAS-361: Config : pool de connection. Suppression des connexions apres un delais d'idle
		routingDataSource.setMaxTotal(Integer.valueOf(env.getProperty("database.client.maxConnections")));
		routingDataSource.setMinIdle(Integer.valueOf(env.getProperty("database.client.minidle")));
		routingDataSource.setMinEvictableIdleTimeMillis(Integer.valueOf(env.getProperty("database.common.eviction.maxtime")));
		routingDataSource.setTimeBetweenEvictionRunsMillis(Integer.valueOf(env.getProperty("database.common.eviction.between")));

		return routingDataSource;
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean() {
		LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
		em.setDataSource(routingDataSource());
		em.setPackagesToScan("com.tryane.saas");
		em.setPersistenceUnitName("saas-data");

		JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		em.setJpaVendorAdapter(vendorAdapter);
		em.setJpaProperties(additionalProperties());

		return em;
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean clientEntityManagerFactoryBean() {
		LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
		em.setDataSource(commonClientDataSource());
		em.setPackagesToScan("com.tryane.saas.core.client", "com.tryane.saas.core.network", "com.tryane.saas.core.user", "com.tryane.saas.core.error", "com.tryane.saas.utils.global", "com.tryane.saas.payment.invoice", "com.tryane.saas.payment.specialoffer");
		em.setPersistenceUnitName("saas-client-data");

		JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		em.setJpaVendorAdapter(vendorAdapter);
		em.setJpaProperties(additionalProperties());

		return em;
	}

	@Bean
	@Primary
	public PlatformTransactionManager transactionManager() {
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(entityManagerFactoryBean().getObject());
		return transactionManager;
	}

	@Bean
	public PlatformTransactionManager clientTransactionManager() {
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(clientEntityManagerFactoryBean().getObject());
		return transactionManager;
	}

	@Bean
	public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
		return new PersistenceExceptionTranslationPostProcessor();
	}

	Properties additionalProperties() {
		Properties properties = new Properties();
		properties.setProperty("hibernate.jdbc.batch_size", "50");
		properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQL9Dialect");
		//properties.setProperty("hibernate.show_sql", "true");
		return properties;
	}
}
