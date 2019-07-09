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
package org.neo4j.driver.springframework.boot.actuate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.neo4j.driver.AccessMode;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.exceptions.SessionExpiredException;
import org.neo4j.driver.internal.SessionConfig;
import org.neo4j.driver.summary.DatabaseInfo;
import org.neo4j.driver.summary.ResultSummary;
import org.neo4j.driver.summary.ServerInfo;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.util.StringUtils;

/**
 * {@link HealthIndicator} that tests the status of a Neo4j by executing a Cypher statement and extracting
 * server and database information.
 *
 * @author Michael J. Simons
 * @soundtrack Iron Maiden - Somewhere In Time
 */
public final class Neo4jHealthIndicator extends AbstractHealthIndicator {

	private static final Log logger = LogFactory.getLog(Neo4jHealthIndicator.class);

	/**
	 * The Cypher statement used to verify Neo4j is up.
	 */
	static final String CYPHER = "RETURN 1 AS result";
	/**
	 * Message indicating that the health check failed.
	 */
	static final String MESSAGE_HEALTH_CHECK_FAILED = "Neo4j health check failed";
	/**
	 * Message logged before retrying a health check.
	 */
	static final String MESSAGE_SESSION_EXPIRED = "Neo4j session has expired, retrying one single time to retrieve server health.";
	/**
	 * The default session config to use while connecting.
	 */
	static final SessionConfig DEFAULT_SESSION_CONFIG = SessionConfig.builder().withDefaultAccessMode(AccessMode.WRITE)
		.build();
	/**
	 * The driver for this health indicator instance.
	 */
	private final Driver driver;

	public Neo4jHealthIndicator(Driver driver) {
		super(MESSAGE_HEALTH_CHECK_FAILED);
		this.driver = driver;
	}

	@Override
	protected void doHealthCheck(Health.Builder builder) {

		try {
			ResultSummary resultSummary;
			// Retry one time when the session has been expired
			try {
				resultSummary = runHealthCheckQuery();
			} catch (SessionExpiredException sessionExpiredException) {
				logger.warn("Neo4j session has expired, retrying one single time to retrieve server health.");
				resultSummary = runHealthCheckQuery();
			}
			buildStatusUp(resultSummary, builder);
		} catch (Exception e) {
			builder.down().withException(e);
		}
	}

	/**
	 * Applies the given {@link ResultSummary} to the {@link Health.Builder builder} without actually calling {@code build}.
	 *
	 * @param resultSummary
	 * @param builder
	 */
	static Health.Builder buildStatusUp(ResultSummary resultSummary, Health.Builder builder) {
		ServerInfo serverInfo = resultSummary.server();
		DatabaseInfo databaseInfo = resultSummary.database();

		builder.up().withDetail("server", serverInfo.version() + "@" + serverInfo.address());

		if (StringUtils.hasText(databaseInfo.name())) {
			builder.withDetail("database", databaseInfo.name());
		}

		return builder;
	}

	ResultSummary runHealthCheckQuery() {
		// We use WRITE here to make sure UP is returned for a server that supports
		// all possible workloads
		try (Session session = driver.session(DEFAULT_SESSION_CONFIG)) {
			ResultSummary resultSummary = session.run(CYPHER).consume();
			return resultSummary;
		}
	}
}
