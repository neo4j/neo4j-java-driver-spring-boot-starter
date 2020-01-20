package org.neo4j.driver.springframework.boot.test.autoconfigure;

import static org.assertj.core.api.Assertions.*;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.springframework.boot.test.autoconfigure.domain.EmptyPackage;
import org.neo4j.driver.summary.ResultSummary;
import org.neo4j.harness.Neo4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Michael J. Simons
 */
@SpringBootTest(classes = EmptyPackage.class)
@EnableNeo4jTestHarness
@ImportAutoConfiguration(Neo4jTestHarnessAutoConfiguration.class)
class Neo4jTestHarnessIT {

	@Test
	void driverShouldBeConnectedToNeo4jTestHarness(@Autowired Neo4j neo4j, @Autowired Driver driver) {

		try (Session session = driver.session()) {
			ResultSummary summary = session.run("RETURN 1 AS result").consume();
			URI uri = neo4j.boltURI();
			assertThat(summary.server().address()).endsWith(String.format("%s:%d", uri.getHost(), uri.getPort()));
		}
	}
}
