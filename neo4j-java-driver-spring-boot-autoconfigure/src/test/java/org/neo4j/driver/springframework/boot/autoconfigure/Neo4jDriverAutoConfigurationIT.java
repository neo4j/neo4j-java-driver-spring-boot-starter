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

import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.springframework.boot.autoconfigure.domain.EmptyPackage;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Logger;

/**
 * @author Michael J. Simons
 */
@SpringBootTest
@Testcontainers
@ContextConfiguration(initializers = Neo4jDriverAutoConfigurationIT.Neo4jContainerBasedTestPropertyProvider.class)
class Neo4jDriverAutoConfigurationIT {

	private static final String SYS_PROPERTY_NEO4J_ACCEPT_COMMERCIAL_EDITION = "SPRING_BOOT_STARTER_NEO4J_ACCEPT_COMMERCIAL_EDITION";
	private static final String SYS_PROPERTY_NEO4J_REPOSITORY = "SPRING_BOOT_STARTER_NEO4J_REPOSITORY";
	private static final String SYS_PROPERTY_NEO4J_VERSION = "SPRING_BOOT_STARTER_NEO4J_VERSION";

	@Container
	private static Neo4jContainer neo4jServer;

	private static final Logger LOGGER = Logger.getLogger(Neo4jDriverAutoConfigurationIT.class.getName());

	static {
		Predicate<String> isNotBlank = s -> !s.trim().isEmpty();

		String repository = Optional.ofNullable(System.getenv(SYS_PROPERTY_NEO4J_REPOSITORY))
				.filter(isNotBlank)
				.orElseGet(() -> System.getProperty(SYS_PROPERTY_NEO4J_REPOSITORY, "neo4j"));

		String imageVersion = Optional.ofNullable(System.getenv(SYS_PROPERTY_NEO4J_VERSION))
				.filter(isNotBlank)
				.orElseGet(() -> System.getProperty(SYS_PROPERTY_NEO4J_VERSION, "3.5.14"));

		String image = repository + ":" + imageVersion;
		LOGGER.info(() -> String.format("Using image %s", image));

		neo4jServer = new Neo4jContainer<>(image)
			.withoutAuthentication()
			.withEnv("NEO4J_ACCEPT_LICENSE_AGREEMENT",
				Optional.ofNullable(System.getenv(SYS_PROPERTY_NEO4J_ACCEPT_COMMERCIAL_EDITION)).filter(isNotBlank)
					.orElse("no"));
	}


	private final Driver driver;

	@Autowired
	Neo4jDriverAutoConfigurationIT(Driver driver) {
		this.driver = driver;
	}

	@Test
	void ensureDriverIsOpen() {

		try (
			Session session = driver.session();
			Transaction tx = session.beginTransaction()
		) {
			Result statementResult = tx.run("MATCH (n:Thing) RETURN n LIMIT 1");
			assertThat(statementResult.hasNext()).isFalse();
			tx.commit();
		}
	}

	static class Neo4jContainerBasedTestPropertyProvider
		implements ApplicationContextInitializer<ConfigurableApplicationContext> {

		@Override
		public void initialize(ConfigurableApplicationContext applicationContext) {
			TestPropertyValues.of(
				"org.neo4j.driver.uri = " + neo4jServer.getBoltUrl(),
				"org.neo4j.driver.authentication.username = neo4j",
				"org.neo4j.driver.authentication.password = " + neo4jServer.getAdminPassword()
			).applyTo(applicationContext.getEnvironment());
		}
	}

	@Configuration(proxyBeanMethods = false)
	@ImportAutoConfiguration(Neo4jDriverAutoConfiguration.class)
	@EntityScan(basePackageClasses = EmptyPackage.class)
	static class TestConfiguration {
	}
}
