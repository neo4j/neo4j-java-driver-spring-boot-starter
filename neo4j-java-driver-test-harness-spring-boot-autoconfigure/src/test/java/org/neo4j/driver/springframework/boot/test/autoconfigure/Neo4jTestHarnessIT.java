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

import static org.assertj.core.api.Assertions.*;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.springframework.boot.test.autoconfigure.domain.EmptyPackage;
import org.neo4j.driver.summary.ResultSummary;
import org.neo4j.harness.ServerControls;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Michael J. Simons
 */
@SpringBootTest(classes = EmptyPackage.class)
@ImportAutoConfiguration({Neo4jTestHarnessAutoConfiguration.class, TestHarnessDriverConfiguration.class})
class Neo4jTestHarnessIT {

	@Test
	void driverShouldBeConnectedToNeo4jTestHarness(@Autowired ServerControls serverControls, @Autowired Driver driver) {

		try (Session session = driver.session()) {
			ResultSummary summary = session.run("RETURN 1 AS result").consume();
			URI uri = serverControls.boltURI();
			assertThat(summary.server().address()).endsWith(String.format("%s:%d", uri.getHost(), uri.getPort()));
		}
	}
}
