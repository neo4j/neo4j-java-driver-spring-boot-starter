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

import static org.neo4j.driver.springframework.boot.actuate.Neo4jHealthIndicator.*;

import reactor.core.publisher.Mono;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.neo4j.driver.AccessMode;
import org.neo4j.driver.Driver;
import org.neo4j.driver.exceptions.SessionExpiredException;
import org.neo4j.driver.reactive.RxSession;
import org.neo4j.driver.summary.ResultSummary;
import org.springframework.boot.actuate.health.AbstractReactiveHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;

/**
 * {@link ReactiveHealthIndicator} that tests the status of a Neo4j by executing a Cypher statement and extracting
 * server and database information.
 *
 * @author Michael J. Simons
 * @soundtrack Iron Maiden - Seventh Son Of A Seventh Son
 */
public final class Neo4jReactiveHealthIndicator extends AbstractReactiveHealthIndicator {

	private static final Log logger = LogFactory.getLog(Neo4jReactiveHealthIndicator.class);

	/**
	 * The driver for this health indicator instance.
	 */
	private final Driver driver;

	public Neo4jReactiveHealthIndicator(Driver driver) {
		this.driver = driver;
	}

	@Override
	protected Mono<Health> doHealthCheck(Health.Builder builder) {
		return runHealthCheckQuery()
			.doOnError(SessionExpiredException.class, e -> logger.warn(MESSAGE_SESSION_EXPIRED))
			.retry(1, SessionExpiredException.class::isInstance)
			.map(r -> buildStatusUp(r, builder).build());
	}

	Mono<ResultSummary> runHealthCheckQuery() {
		// We use WRITE here to make sure UP is returned for a server that supports
		// all possible workloads
		return Mono.using(
			() -> driver.rxSession(p -> p.withDefaultAccessMode(AccessMode.WRITE)),
			session -> Mono.from(session.run(Neo4jHealthIndicator.CYPHER).summary()),
			RxSession::close
		);
	}
}
