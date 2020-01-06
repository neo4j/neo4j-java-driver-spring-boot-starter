/*
 * Copyright (c) 2019-2020 "Neo4j,"
 * Neo4j Sweden AB [https://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.driver.springframework.boot.autoconfigure;

import java.util.List;

import org.neo4j.driver.Driver;
import org.neo4j.ogm.config.AutoIndexMode;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.event.EventListener;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jProperties;
import org.springframework.boot.autoconfigure.domain.EntityScanPackages;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Our starter will pull in this prior to Spring Boots Neo4j Data configuration and configure OGM directly through
 * the driver if possible.
 *
 * @author Michael J. Simons
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(Driver.class)
@ConditionalOnMissingBean(SessionFactory.class)
@ConditionalOnClass({ SessionFactory.class, BoltDriver.class })
@EnableConfigurationProperties(Neo4jProperties.class)
class AdditionalDataConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public org.neo4j.ogm.config.Configuration configuration(Neo4jProperties properties) {
		return properties.createConfiguration();
	}

	@Bean
	@ConditionalOnMissingBean
	public BoltDriver ogmDriver(org.neo4j.ogm.config.Configuration ogmConfiguration, Driver nativeDriver) {

		BoltDriver boltDriver = new BoltDriver(nativeDriver) {
			@Override
			public synchronized void close() {
				// We must prevent the bolt driver from closing the driver bean
			}
		};
		boltDriver.configure(ogmConfiguration);
		return boltDriver;
	}

	@Bean
	@ConditionalOnBean(BoltDriver.class)
	public SessionFactory sessionFactory(org.neo4j.ogm.config.Configuration configuration, BoltDriver ogmDriver,
		ApplicationContext applicationContext, ObjectProvider<EventListener> eventListenerProvider) {

		String[] packagesToScan = getPackagesToScan(applicationContext);
		SessionFactory sessionFactory = new SessionFactory(ogmDriver, packagesToScan);
		if (configuration.getAutoIndex() != AutoIndexMode.NONE) {
			sessionFactory.runAutoIndexManager(configuration);
		}

		eventListenerProvider.stream().forEach(sessionFactory::register);
		return sessionFactory;
	}

	private String[] getPackagesToScan(ApplicationContext applicationContext) {
		List<String> packages = EntityScanPackages.get(applicationContext).getPackageNames();
		if (packages.isEmpty() && AutoConfigurationPackages.has(applicationContext)) {
			packages = AutoConfigurationPackages.get(applicationContext);
		}
		return StringUtils.toStringArray(packages);
	}

}
