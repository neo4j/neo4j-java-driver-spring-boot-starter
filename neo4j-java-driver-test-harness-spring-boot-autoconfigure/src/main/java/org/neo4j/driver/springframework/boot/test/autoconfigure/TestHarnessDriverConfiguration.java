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
package org.neo4j.driver.springframework.boot.test.autoconfigure;

import static org.neo4j.driver.springframework.boot.test.autoconfigure.Neo4jTestHarnessAutoConfiguration.*;

import java.lang.reflect.Method;
import java.net.URI;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.springframework.boot.autoconfigure.Neo4jDriverProperties;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Automatic configuration that provides a driver that is connected to an existing instance of a Neo4j test harness.
 * when there's no driver configured elsewhere.
 *
 * @author Michael J. Simons
 * @soundtrack NMZS - Egotrip
 * @since 4.0.0.1
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(Neo4jDriverProperties.class)
class TestHarnessDriverConfiguration {

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(name = TEST_HARNESS_3X)
	@ConditionalOnBean(type = TEST_HARNESS_3X)
	static class ServerControlsConfiguration {

		@Bean
		Driver neo4jDriver(final Neo4jDriverProperties driverProperties, final ListableBeanFactory beanFactory) {

			return createDriverFor(driverProperties, beanFactory, TEST_HARNESS_3X);
		}

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(name = TEST_HARNESS_4X)
	@ConditionalOnBean(type = TEST_HARNESS_4X)
	static class Neo4jConfiguration {

		@Bean
		Driver neo4jDriver(final Neo4jDriverProperties driverProperties, final ListableBeanFactory beanFactory) {

			return createDriverFor(driverProperties, beanFactory, TEST_HARNESS_4X);
		}
	}

	private static Driver createDriverFor(
		final Neo4jDriverProperties driverProperties,
		final ListableBeanFactory beanFactory, String embeddedServerClassName
	) {
		LOG.debug("Creating a driver instance connected against Neo4j test harness.");

		Class<?> embeddedServerClass = ClassUtils
			.resolveClassName(embeddedServerClassName, ClassUtils.getDefaultClassLoader());
		Object embeddedServerInstance = beanFactory.getBean(embeddedServerClass);
		Method boltURI = ReflectionUtils.findMethod(embeddedServerClass, "boltURI");

		return GraphDatabase.driver(
			(URI) ReflectionUtils.invokeMethod(boltURI, embeddedServerInstance),
			AuthTokens.none(),
			driverProperties.asDriverConfig());
	}
}
