/*
 * Copyright (c) 2019 "Neo4j,"
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

import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;

import org.neo4j.driver.springframework.boot.autoconfigure.domain.EmptyPackage;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.transaction.Neo4jTransactionManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.neo4j.driver.springframework.boot.test.Neo4jDriverMocks.*;

/**
 * @author Michael J. Simons
 */
class Neo4jDriverAutoConfigurationTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(Neo4jDriverAutoConfiguration.class));

	@Test
	void shouldRequireAllNeededClasses() {

		this.contextRunner
			.withPropertyValues("org.neo4j.driver.uri=bolt://localhost:4711")
			.withClassLoader(new FilteredClassLoader(Driver.class))
			.run((ctx) -> assertThat(ctx).doesNotHaveBean(Driver.class));
	}

	@Test
	void shouldRequireUri() {

		this.contextRunner
			.run((ctx) -> assertThat(ctx)
				.doesNotHaveBean(Driver.class)
			);
	}

	@Test
	void shouldCreateDriver() {

		this.contextRunner
			.withPropertyValues("org.neo4j.driver.uri=bolt://localhost:4711")
			.withClassLoader(new FilteredClassLoader(SessionFactory.class))
			.run((ctx) -> assertThat(ctx)
				.hasSingleBean(Driver.class)
			);
	}

	@Test
	void shouldAlsoCreateOGMBeans() {

		this.contextRunner
			.withConfiguration(AutoConfigurations.of(Neo4jDataAutoConfiguration.class))
			.withUserConfiguration(TestConfiguration.class, WithDriver.class)
			.run((ctx) -> assertThat(ctx)
				.hasSingleBean(Driver.class)
				.hasSingleBean(BoltDriver.class)
				.hasSingleBean(SessionFactory.class)
				.hasSingleBean(Neo4jTransactionManager.class) // See https://github.com/spring-projects/spring-boot/pull/17662
			);
	}

	// Needed to not make OGM go mad on package root
	@Configuration(proxyBeanMethods = false)
	@EntityScan(basePackageClasses = EmptyPackage.class)
	static class TestConfiguration {
	}

	// Mock the driver for OGM related test
	@Configuration(proxyBeanMethods = false)
	static class WithDriver {

		@Bean
		Driver driver() {
			return mockDriverWithoutMetrics();
		}
	}
}
