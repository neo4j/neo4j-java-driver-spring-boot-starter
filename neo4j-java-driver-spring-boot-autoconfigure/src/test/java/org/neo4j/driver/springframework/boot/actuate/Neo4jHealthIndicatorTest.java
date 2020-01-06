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
package org.neo4j.driver.springframework.boot.actuate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.neo4j.driver.v1.AccessMode;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.exceptions.ServiceUnavailableException;
import org.neo4j.driver.v1.exceptions.SessionExpiredException;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

/**
 * @author Michael J. Simons
 */
@ExtendWith(MockitoExtension.class)
class Neo4jHealthIndicatorTest extends Neo4jHealthIndicatorTestBase {

	@Mock
	private Session session;

	@Mock
	private StatementResult statementResult;

	@Test
	void shouldWorkWithoutDatabaseName() {
		when(this.serverInfo.version()).thenReturn("4711");
		when(this.serverInfo.address()).thenReturn("Zu Hause");
		when(this.resultSummary.server()).thenReturn(this.serverInfo);

		when(this.statementResult.consume()).thenReturn(this.resultSummary);
		when(this.session.run(anyString())).thenReturn(this.statementResult);

		when(this.driver.session(AccessMode.WRITE)).thenReturn(this.session);

		Neo4jHealthIndicator healthIndicator = new Neo4jHealthIndicator(this.driver);
		Health health = healthIndicator.health();
		assertThat(health.getStatus()).isEqualTo(Status.UP);
		assertThat(health.getDetails()).containsEntry("server", "4711@Zu Hause");
	}

	@Test
	void shouldWorkWithEmptyDatabaseName() {
		when(this.serverInfo.version()).thenReturn("4711");
		when(this.serverInfo.address()).thenReturn("Zu Hause");
		when(this.resultSummary.server()).thenReturn(this.serverInfo);

		when(this.statementResult.consume()).thenReturn(this.resultSummary);
		when(this.session.run(anyString())).thenReturn(this.statementResult);

		when(this.driver.session(AccessMode.WRITE)).thenReturn(this.session);

		Neo4jHealthIndicator healthIndicator = new Neo4jHealthIndicator(this.driver);
		Health health = healthIndicator.health();
		assertThat(health.getStatus()).isEqualTo(Status.UP);
		assertThat(health.getDetails()).containsEntry("server", "4711@Zu Hause");
	}

	@Test
	void neo4jIsUp() {

		prepareSharedMocks();
		when(this.statementResult.consume()).thenReturn(this.resultSummary);
		when(this.session.run(anyString())).thenReturn(this.statementResult);

		when(this.driver.session(AccessMode.WRITE)).thenReturn(this.session);

		Neo4jHealthIndicator healthIndicator = new Neo4jHealthIndicator(this.driver);
		Health health = healthIndicator.health();
		assertThat(health.getStatus()).isEqualTo(Status.UP);
		assertThat(health.getDetails()).containsEntry("server", "4711@Zu Hause");

		verify(session).close();
		verifyNoMoreInteractions(this.driver, this.session, this.statementResult, this.resultSummary, this.serverInfo);
	}

	@Test
	void neo4jSessionIsExpiredOnce() {

		AtomicInteger cnt = new AtomicInteger(0);

		prepareSharedMocks();
		when(this.statementResult.consume()).thenReturn(this.resultSummary);
		when(this.session.run(anyString())).thenAnswer(invocation -> {
			if (cnt.compareAndSet(0, 1)) {
				throw new SessionExpiredException("Session expired");
			}
			return Neo4jHealthIndicatorTest.this.statementResult;
		});
		when(this.driver.session(AccessMode.WRITE)).thenReturn(this.session);

		Neo4jHealthIndicator healthIndicator = new Neo4jHealthIndicator(this.driver);
		Health health = healthIndicator.health();

		assertThat(health.getStatus()).isEqualTo(Status.UP);
		assertThat(health.getDetails()).containsEntry("server", "4711@Zu Hause");

		verify(this.session, times(2)).close();
		verifyNoMoreInteractions(this.driver, this.session, this.statementResult, this.resultSummary, this.serverInfo);
	}

	@Test
	void neo4jSessionIsDown() {

		when(driver.session(AccessMode.WRITE)).thenThrow(ServiceUnavailableException.class);

		Neo4jHealthIndicator healthIndicator = new Neo4jHealthIndicator(driver);
		Health health = healthIndicator.health();

		assertThat(health.getStatus()).isEqualTo(Status.DOWN);
		assertThat(health.getDetails()).containsKeys("error");

		verifyNoMoreInteractions(this.driver, this.session, this.statementResult, this.resultSummary, this.serverInfo);
	}
}
