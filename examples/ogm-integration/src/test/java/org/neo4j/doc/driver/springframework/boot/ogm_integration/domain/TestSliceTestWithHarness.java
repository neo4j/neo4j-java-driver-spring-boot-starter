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

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.driver.springframework.boot.autoconfigure.Neo4jDriverAutoConfiguration;
import org.neo4j.driver.springframework.boot.test.autoconfigure.Neo4jTestHarnessAutoConfiguration;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * This tests shows that the Neo4j-OGM with the Java Driver autoconfiguration + test harness works fine with test slices.
 *
 * @author Michael J. Simons
 * @soundtrack Katey Sagal - Covered
 */
@DataNeo4jTest
// This is necessary, as the test slice cannot know about it
@ImportAutoConfiguration({ Neo4jTestHarnessAutoConfiguration.class, Neo4jDriverAutoConfiguration.class })
class TestSliceTestWithHarness {

	@TestConfiguration
	static class TestHarnessConfig {

		@Bean
		public ServerControls neo4j() {
			return TestServerBuilders.newInProcessBuilder()
				.withFixture(""
					+ "CREATE (TheMatrix:Movie {title:'The Matrix', released:1999, tagline:'Welcome to the Real World'})\n"
					+ "CREATE (TheMatrixReloaded:Movie {title:'The Matrix Reloaded', released:2003, tagline:'Free your mind'})\n"
					+ "CREATE (TheMatrixRevolutions:Movie {title:'The Matrix Revolutions', released:2003, tagline:'Everything that has a beginning has an end'})\n"
				)
				.newServer();
		}
	}

	@Test
	public void sessionFactoryShouldUseOGMBoltDriver(@Autowired Driver driver,
		@Autowired SessionFactory sessionFactory) {
		assertThat(sessionFactory.unwrap(org.neo4j.ogm.driver.Driver.class)).isInstanceOf(BoltDriver.class);

		BoltDriver boltDriver = (BoltDriver) sessionFactory.unwrap(org.neo4j.ogm.driver.Driver.class);
		assertThat(boltDriver.unwrap(Driver.class)).isSameAs(driver);
	}

	@Test
	public void repositoriesShouldWork(@Autowired MovieRepository movieRepository) {

		MovieEntity movieEntity = movieRepository.save(new MovieEntity("Life of Brian"));
		assertThat(movieEntity.getId()).isNotNull();

		String title = "The Matrix";
		Optional<MovieEntity> theMatrix = movieRepository.findByTitle(title);
		assertThat(theMatrix).isPresent().map(MovieEntity::getTitle).hasValue(title);
	}
}
