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
package org.neo4j.doc.driver.springframework.boot.embedded;

// tag::test[]
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.SocketUtils;

// end::test[]

/**
 * @author Michael J. Simons
 */
// tag::test[]
@SpringBootTest // <.>
class MoviesServiceTest {

	@DynamicPropertySource // <.>
	static void neo4jProperties(DynamicPropertyRegistry registry) {

		// Configure a free port
		registry.add("org.neo4j.driver.uri", () -> "bolt://localhost:" + SocketUtils.findAvailableTcpPort()); // <.>

		// You could also point the embedded instance to some pre-seeded directory <.>
		// registry.add(
		// 	"org.neo4j.database-management-service.home-directory",
		// 	() -> "/Users/msimons/tmp/preseededNeo4j"
		// );
	}

	// end::test[]

	/**
	 * You can inject the database management service already in a static method.
	 *
	 * @param databaseManagementService The embedded database management service
	 */
	// tag::test[]
	@BeforeAll // <.>
	static void prepareTestData(
		@Autowired DatabaseManagementService databaseManagementService
	) {

		try (var tx = databaseManagementService.database("neo4j").beginTx()) {
			tx.execute(
				"CREATE (TheMatrix:Movie {title:'The Matrix', released:1999, tagline:'Welcome to the Real World'})\n"
					+ "CREATE (TheMatrixReloaded:Movie {title:'The Matrix Reloaded', released:2003, tagline:'Free your mind'})\n"
					+ "CREATE (TheMatrixRevolutions:Movie {title:'The Matrix Revolutions', released:2003, tagline:'Everything that has a beginning has an end'})\n");
			tx.commit();
		}
	}

	// end::test[]

	/**
	 * Test your domain, here our movie service. You can also inject the driver again and do stuff with it.
	 *
	 * @param moviesService
	 */
	// tag::test[]
	@Test
	void shouldRetrieveMovieTitles(@Autowired MoviesService moviesService) { // <.>

		assertThat(moviesService.getMovieTitles())
			.hasSize(3)
			.contains("The Matrix");
	}
}
// end::test[]

