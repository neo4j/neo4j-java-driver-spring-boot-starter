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

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.neo4j.driver.exceptions.ServiceUnavailableException;
import org.neo4j.driver.exceptions.SessionExpiredException;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.reactive.RxResult;
import org.neo4j.driver.reactive.RxSession;
import org.neo4j.driver.reactive.RxTransaction;
import org.neo4j.driver.reactive.RxTransactionWork;
import org.springframework.boot.actuate.health.Status;

/**
 * @author Michael J. Simons
 */
@ExtendWith(MockitoExtension.class)
class Neo4jReactiveHealthIndicatorTest extends Neo4jHealthIndicatorTestBase {

	@Mock
	private RxSession session;

	@Mock
	private RxResult statementResult;

	@Mock
	private RxTransaction transaction;

	@Test
	void neo4jIsUp() {

		prepareSharedMocks();
		when(statementResult.records()).thenReturn(Mono.just(record));
		when(statementResult.consume()).thenReturn(Mono.just(resultSummary));
		when(transaction.run(anyString())).thenReturn(this.statementResult);
		when(session.writeTransaction(any(RxTransactionWork.class))).then(invocationOnMock -> {
			RxTransactionWork<ResultSummaryWithEdition> tw = invocationOnMock.getArgument(0);
			return tw.execute(transaction);
		});
		when(session.close()).thenReturn(Mono.empty());

		when(driver.rxSession(any(SessionConfig.class))).thenReturn(session);

		Neo4jReactiveHealthIndicator healthIndicator = new Neo4jReactiveHealthIndicator(driver);
		healthIndicator
			.health()
			.as(StepVerifier::create)
			.consumeNextWith(health -> {
				assertThat(health.getStatus()).isEqualTo(Status.UP);
				assertThat(health.getDetails()).containsEntry("server", "4711@Zu Hause");
				assertThat(health.getDetails()).containsEntry("edition", "ultimate collectors edition");
			})
			.verifyComplete();

		verify(session).close();
		verifyNoMoreInteractions(driver, session, statementResult, resultSummary, serverInfo, databaseInfo, transaction);
	}

	@Test
	void neo4jSessionIsExpiredOnce() {

		AtomicInteger cnt = new AtomicInteger(0);

		prepareSharedMocks();
		when(statementResult.records()).thenReturn(Mono.just(record));
		when(statementResult.consume()).thenReturn(Mono.just(resultSummary));
		when(transaction.run(anyString())).thenAnswer(invocation -> {
			if (cnt.compareAndSet(0, 1)) {
				throw new SessionExpiredException("Session expired");
			}
			return statementResult;
		});
		when(session.writeTransaction(any(RxTransactionWork.class))).then(invocationOnMock -> {
			RxTransactionWork<ResultSummaryWithEdition> tw = invocationOnMock.getArgument(0);
			return tw.execute(transaction);
		});
		when(session.close()).thenReturn(Mono.empty());

		when(driver.rxSession(any(SessionConfig.class))).thenReturn(session);

		Neo4jReactiveHealthIndicator healthIndicator = new Neo4jReactiveHealthIndicator(driver);
		healthIndicator
			.health()
			.as(StepVerifier::create)
			.consumeNextWith(health -> {
				assertThat(health.getStatus()).isEqualTo(Status.UP);
				assertThat(health.getDetails()).containsEntry("server", "4711@Zu Hause");
				assertThat(health.getDetails()).containsEntry("edition", "ultimate collectors edition");
			})
			.verifyComplete();

		verify(session, times(2)).close();
		verifyNoMoreInteractions(driver, session, statementResult, resultSummary, serverInfo, databaseInfo, transaction);
	}

	@Test
	void neo4jSessionIsDown() {

		when(driver.rxSession(any(SessionConfig.class))).thenThrow(ServiceUnavailableException.class);

		Neo4jReactiveHealthIndicator healthIndicator = new Neo4jReactiveHealthIndicator(driver);
		healthIndicator
			.health()
			.as(StepVerifier::create)
			.consumeNextWith(health -> {
				assertThat(health.getStatus()).isEqualTo(Status.DOWN);
				assertThat(health.getDetails()).containsKeys("error");
			})
			.verifyComplete();

		verifyNoMoreInteractions(driver, session, statementResult, resultSummary, serverInfo, databaseInfo, transaction);
	}
}
