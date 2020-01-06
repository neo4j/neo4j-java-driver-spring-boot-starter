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
import static org.neo4j.driver.springframework.boot.actuate.Neo4jDriverMetrics.*;
import static org.neo4j.driver.springframework.boot.test.Neo4jDriverMocks.*;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;

/**
 * @author Michael J. Simons
 */
class Neo4jDriverMetricsTest {

	@Test
	void shouldDetectEnabledMetrics() {

		Driver driver = mockDriverWithMetrics();
		assertThat(Neo4jDriverMetrics.metricsAreEnabled(driver)).isTrue();
	}

	@Test
	void shouldDetectDisabledMetrics() {

		Driver driver = mockDriverWithoutMetrics();
		assertThat(Neo4jDriverMetrics.metricsAreEnabled(driver)).isFalse();
	}

	@Test
	void shouldRegisterCorrectMeters() {

		SimpleMeterRegistry registry = new SimpleMeterRegistry();
		Neo4jDriverMetrics metrics = new Neo4jDriverMetrics("driver", mockDriverWithMetrics(), Collections.emptyList());
		metrics.bindTo(registry);

		assertThat(registry.get(PREFIX + ".acquired").functionCounter()).isNotNull();
		assertThat(registry.get(PREFIX + ".closed").functionCounter()).isNotNull();
		assertThat(registry.get(PREFIX + ".created").functionCounter()).isNotNull();
		assertThat(registry.get(PREFIX + ".failedToCreate").functionCounter()).isNotNull();
		assertThat(registry.get(PREFIX + ".idle").gauge()).isNotNull();
		assertThat(registry.get(PREFIX + ".inUse").gauge()).isNotNull();
		assertThat(registry.get(PREFIX + ".timedOutToAcquire").functionCounter()).isNotNull();
	}
}
