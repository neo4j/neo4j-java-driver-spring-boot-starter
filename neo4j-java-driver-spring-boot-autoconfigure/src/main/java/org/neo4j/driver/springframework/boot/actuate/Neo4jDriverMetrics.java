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

import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;

import java.util.function.Consumer;

import org.neo4j.driver.ConnectionPoolMetrics;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Metrics;
import org.neo4j.driver.exceptions.ClientException;
import org.springframework.util.Assert;

/**
 * This is a {@link MeterBinder} that binds all available Neo4j driver metrics
 * to Micrometer.
 *
 * @author Michael J. Simons
 */
public final class Neo4jDriverMetrics implements MeterBinder {

	public static final String PREFIX = "neo4j.driver.connections";
	private static final String BASE_UNIT_CONNECTIONS = "connections";

	private final Driver driver;

	private final Iterable<Tag> tags;

	public Neo4jDriverMetrics(String name, Driver driver, Iterable<Tag> tags) {

		Assert.notNull(name, "Bean name must not be null");
		Assert.notNull(driver, "Driver must not be null");
		Assert.notNull(tags, "Tags must not be null (but may be empty)");
		this.driver = driver;
		this.tags = Tags.concat(tags, "name", name);
	}

	@Override
	public void bindTo(MeterRegistry meterRegistry) {

		Metrics metrics = driver.metrics();
		metrics.connectionPoolMetrics().forEach(this.getPoolMetricsBinder(meterRegistry));
	}

	Consumer<ConnectionPoolMetrics> getPoolMetricsBinder(MeterRegistry meterRegistry) {
		return poolMetrics -> {
			Iterable<Tag> poolTags = Tags.concat(tags, "poolId", poolMetrics.id());

			FunctionCounter.builder(PREFIX + ".acquired", poolMetrics, ConnectionPoolMetrics::acquired)
				.tags(poolTags)
				.baseUnit(BASE_UNIT_CONNECTIONS)
				.description("The amount of connections that have been acquired.")
				.register(meterRegistry);

			FunctionCounter.builder(PREFIX + ".closed", poolMetrics, ConnectionPoolMetrics::closed)
				.tags(poolTags)
				.baseUnit(BASE_UNIT_CONNECTIONS)
				.description("The amount of connections have been closed.")
				.register(meterRegistry);

			FunctionCounter.builder(PREFIX + ".created", poolMetrics, ConnectionPoolMetrics::created)
				.tags(poolTags)
				.baseUnit(BASE_UNIT_CONNECTIONS)
				.description("The amount of connections have ever been created.")
				.register(meterRegistry);

			FunctionCounter.builder(PREFIX + ".failedToCreate", poolMetrics, ConnectionPoolMetrics::failedToCreate)
				.tags(poolTags)
				.baseUnit(BASE_UNIT_CONNECTIONS)
				.description("The amount of connections have been failed to create.")
				.register(meterRegistry);

			Gauge.builder(PREFIX + ".idle", poolMetrics, ConnectionPoolMetrics::idle)
				.tags(poolTags)
				.baseUnit(BASE_UNIT_CONNECTIONS)
				.description("The amount of connections that are currently idle.")
				.register(meterRegistry);

			Gauge.builder(PREFIX + ".inUse", poolMetrics, ConnectionPoolMetrics::inUse)
				.tags(poolTags)
				.baseUnit(BASE_UNIT_CONNECTIONS)
				.description("The amount of connections that are currently in-use.")
				.register(meterRegistry);

			FunctionCounter
				.builder(PREFIX + ".timedOutToAcquire", poolMetrics, ConnectionPoolMetrics::timedOutToAcquire)
				.tags(poolTags)
				.baseUnit(BASE_UNIT_CONNECTIONS)
				.description(
					"The amount of failures to acquire a connection from a pool within maximum connection acquisition timeout.")
				.register(meterRegistry);
		};
	}

	/**
	 * @param driver The driver instance beans to check whether it has metrics enabled.
	 * @return True, if the given bean exposes metrics
	 */
	public static boolean metricsAreEnabled(Driver driver) {

		try {
			driver.metrics();
			return true;
		} catch (ClientException e) {
			return false;
		}
	}
}
