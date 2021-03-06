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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.driver.springframework.boot.test.autoconfigure.Neo4jTestHarnessAutoConfiguration;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * This variant uses a {@link DynamicPropertySource @ DynamicPropertySource} for providing dynamic values applicable in tests. 
 * Thus, the autoconfiguration of the driver is kept and all other things
 * are as you'd expect in production.
 * <p>If you don't like that setup, look at {@link MoviesServiceAlt1Test}. There we expose the embedded server as a Spring Bean
 * and don't do the manual connection setting.
 */
// tag::test-harness-example-option4[]
@SpringBootTest
@EnableAutoConfiguration(exclude = { Neo4jTestHarnessAutoConfiguration.class })
class MoviesServiceAlt4Test {

	private static Neo4j embeddedDatabaseServer;

	@BeforeAll
	static void initializeNeo4j() {

		embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
			.withDisabledServer() // No need for http
			.withFixture(""
				+ "CREATE (TheMatrix:Movie {title:'The Matrix', released:1999, tagline:'Welcome to the Real World'})\n"
				+ "CREATE (TheMatrixReloaded:Movie {title:'The Matrix Reloaded', released:2003, tagline:'Free your mind'})\n"
				+ "CREATE (TheMatrixRevolutions:Movie {title:'The Matrix Revolutions', released:2003, tagline:'Everything that has a beginning has an end'})\n"
			)
			.build();
	}


	@DynamicPropertySource
	static void neo4jProperties(DynamicPropertyRegistry registry) {
		registry.add("org.neo4j.driver.uri", embeddedDatabaseServer::boltURI);
		registry.add("org.neo4j.driver.authentication.password", () -> "");
	}

	@AfterAll
	static void closeNeo4j() {
		embeddedDatabaseServer.close();
	}

	@Test
	void testSomethingWithTheDriver(@Autowired Driver driver) {
	}
	// end::test-harness-example-option4[]

	@Test
	void shouldRetrieveMovieTitles(@Autowired MoviesService moviesService) {

		assertThat(moviesService.getMovieTitles())
			.hasSize(3)
			.contains("The Matrix");
	}
	// tag::test-harness-example-option4[]
}
// end::test-harness-example-option4[]
