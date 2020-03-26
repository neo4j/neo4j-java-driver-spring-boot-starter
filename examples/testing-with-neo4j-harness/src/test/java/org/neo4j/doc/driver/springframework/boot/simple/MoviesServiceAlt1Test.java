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
package org.neo4j.doc.driver.springframework.boot.simple;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * This variant creates a custom instance of the test harness and exposing it as a bean. There are a couple of ways to do this,
 * this is just one of them. With `neo4j-java-driver-spring-boot-test-harness-4x-support` on the class path, the automatic configuration will pick this up.
 * <p>If you already have the harness support on the classpath, this would actually be the recommended version of doing things.
 */
// tag::test-harness-example-option1[]
@SpringBootTest
class MoviesServiceAlt1Test {

	@TestConfiguration // <.>
	static class TestHarnessConfig {

		@Bean // <.>
		public Neo4j neo4j() {
			return Neo4jBuilders.newInProcessBuilder()
				.withDisabledServer() // No need for http
				.withFixture(""
					+ "CREATE (TheMatrix:Movie {title:'The Matrix', released:1999, tagline:'Welcome to the Real World'})\n"
					+ "CREATE (TheMatrixReloaded:Movie {title:'The Matrix Reloaded', released:2003, tagline:'Free your mind'})\n"
					+ "CREATE (TheMatrixRevolutions:Movie {title:'The Matrix Revolutions', released:2003, tagline:'Everything that has a beginning has an end'})\n"
				)
				.build();

			// For enterprise use
			// return com.neo4j.harness.EnterpriseNeo4jBuilders.newInProcessBuilder()
			//    .newInProcessBuilder()
			//    .build();
		}
	}

	@Test
	void testSomethingWithTheDriver(@Autowired Driver driver) {
	}
	// end::test-harness-example-option1[]

	@Test
	void shouldRetrieveMovieTitles(@Autowired MoviesService moviesService) {

		assertThat(moviesService.getMovieTitles())
			.hasSize(3)
			.contains("The Matrix");
	}

	// tag::test-harness-example-option1[]
}
// end::test-harness-example-option1[]