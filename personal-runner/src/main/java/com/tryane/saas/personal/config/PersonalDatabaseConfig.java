package com.tryane.saas.personal.config;

import java.util.Properties;

import javax.sql.DataSource;

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

import com.tryane.saas.core.config.db.RoutingDataSourceFactory;
import com.tryane.saas.utils.datasource.DataSourceFactory;
import com.tryane.saas.utils.hibernate.HibernateConfig;

@Configuration
@EnableTransactionManagement
public class PersonalDatabaseConfig {
	@Autowired
	Environment env;

	@Bean
	public DataSource commonClientDataSource() {
		return DataSourceFactory.createClientBasicDataSource(env);
	}

	@Bean
	public DataSource routingDataSource() {
		return RoutingDataSourceFactory.createRoutingDataSource(env, commonClientDataSource());
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
		em.setPackagesToScan(HibernateConfig.getCommonEntityManagerPackagesToScan());
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
		return HibernateConfig.getAdditionalProperties(env);
	}
}
