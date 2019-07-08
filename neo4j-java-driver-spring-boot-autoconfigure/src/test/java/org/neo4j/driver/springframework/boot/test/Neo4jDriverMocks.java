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
package org.neo4j.driver.springframework.boot.test;

import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.neo4j.driver.ConnectionPoolMetrics;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Metrics;
import org.neo4j.driver.exceptions.ClientException;

/**
 * Some predefined mocks, only to be used internally for tests.
 *
 * @author Michael J. Simons
 */
public final class Neo4jDriverMocks {

	public static Driver mockDriverWithMetrics() {
		ConnectionPoolMetrics p1 = mock(ConnectionPoolMetrics.class);

		Map<String, ConnectionPoolMetrics> connectionPoolMetrics = new HashMap<>();
		connectionPoolMetrics.put("p1", p1);

		Metrics metrics = mock(Metrics.class);
		when(metrics.connectionPoolMetrics()).thenReturn(connectionPoolMetrics);

		Driver driver = mock(Driver.class);
		when(driver.metrics()).thenReturn(metrics);

		when(driver.verifyConnectivityAsync()).thenReturn(CompletableFuture.completedFuture(null));

		return driver;
	}

	public static Driver mockDriverWithoutMetrics() {

		Driver driver = mock(Driver.class);
		when(driver.metrics()).thenThrow(ClientException.class);
		return driver;
	}

	private Neo4jDriverMocks() {
	}
}
