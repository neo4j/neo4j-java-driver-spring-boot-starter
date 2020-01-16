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
package org.neo4j.doc.driver.springframework.boot.ogm_integration.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.neo4j.driver.springframework.boot.autoconfigure.Neo4jDriverAutoConfiguration;
import org.neo4j.driver.Driver;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * This tests shows that the Neo4j-OGM with the Java Driver autoconfiguration works fine with test slices.
 *
 * @author Michael J. Simons
 */
@Testcontainers
@DataNeo4jTest
@ImportAutoConfiguration(Neo4jDriverAutoConfiguration.class) // This is necessary, as the test slice cannot know about it
@ContextConfiguration(initializers = { TestSliceTest.Initializer.class })
class TestSliceTest {

	@Container
	private static final Neo4jContainer neo4jContainer = new Neo4jContainer<>();

	@Autowired
	private Driver driver;

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private MovieRepository movieRepository;

	@Test
	public void sessionFactoryShouldUseOGMBoltDriver() {
		assertThat(sessionFactory.unwrap(org.neo4j.ogm.driver.Driver.class)).isInstanceOf(BoltDriver.class);

		BoltDriver boltDriver = (BoltDriver) sessionFactory.unwrap(org.neo4j.ogm.driver.Driver.class);
		assertThat(boltDriver.unwrap(org.neo4j.driver.Driver.class)).isSameAs(this.driver);
	}

	@Test
	public void repositoriesShouldWork() {

		MovieEntity movieEntity = movieRepository.save(new MovieEntity("Life of Brian"));
		assertThat(movieEntity.getId()).isNotNull();

		// We cannot compare here with a direct query result from the driver as the data-slice test runs in a transaction
		// which is rolled back by default and we cannot join it based with the pure driver.
	}

	static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {

			TestPropertyValues.of(
				"org.neo4j.driver.uri=" + neo4jContainer.getBoltUrl(),
				"org.neo4j.driver.authentication.username=neo4j",
				"org.neo4j.driver.authentication.password=" + neo4jContainer.getAdminPassword()
			).applyTo(configurableApplicationContext.getEnvironment());
		}
	}
}
