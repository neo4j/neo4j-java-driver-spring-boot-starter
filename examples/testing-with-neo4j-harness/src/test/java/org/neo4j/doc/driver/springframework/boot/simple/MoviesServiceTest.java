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
package org.neo4j.doc.driver.springframework.boot.simple;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;

/**
 * This variant uses a custom {@link ApplicationContextInitializer} that modifies Springs configuration properties
 * with the help of {@link TestPropertyValues}. Thus, the autoconfiguration of the driver is kept and all other things
 * are as you'd expect in production.
 * <strong>This would be our recommended setup!</strong> There are some more options to pass properties into the testconfig,
 * use one of them, if this here isn't your thing.
 */
@SpringBootTest
@ContextConfiguration(initializers = { MoviesServiceTest.Initializer.class })
class MoviesServiceTest {

	private static ServerControls embeddedDatabaseServer;

	@BeforeAll
	static void initializeNeo4j() {

		embeddedDatabaseServer = TestServerBuilders.newInProcessBuilder()
			.withFixture(""
				+ "CREATE (TheMatrix:Movie {title:'The Matrix', released:1999, tagline:'Welcome to the Real World'})\n"
				+ "CREATE (TheMatrixReloaded:Movie {title:'The Matrix Reloaded', released:2003, tagline:'Free your mind'})\n"
				+ "CREATE (TheMatrixRevolutions:Movie {title:'The Matrix Revolutions', released:2003, tagline:'Everything that has a beginning has an end'})\n"
			)
			.newServer();
	}

	@AfterAll
	static void closeNeo4j() {
		embeddedDatabaseServer.close();
	}

	static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {

			TestPropertyValues.of(
				"org.neo4j.driver.uri=" + embeddedDatabaseServer.boltURI().toString(),
				"org.neo4j.driver.authentication.password="
			).applyTo(configurableApplicationContext.getEnvironment());
		}
	}

	@Autowired
	private MoviesService moviesService;

	@Test
	void shouldRetrieveMovieTitles() {

		assertThat(moviesService.getMovieTitles())
			.hasSize(3)
			.contains("The Matrix");
	}
}
