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
package org.neo4j.driver.springframework.boot.autoconfigure;

import reactor.core.publisher.Flux;

import java.util.Map;

import org.neo4j.driver.Driver;
import org.neo4j.driver.springframework.boot.actuate.Neo4jHealthIndicator;
import org.neo4j.driver.springframework.boot.actuate.Neo4jReactiveHealthIndicator;
import org.springframework.boot.actuate.autoconfigure.health.CompositeHealthIndicatorConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.CompositeReactiveHealthIndicatorConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.autoconfigure.health.HealthIndicatorAutoConfiguration;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for health indicators on all available
 * {@link org.neo4j.driver.Driver drivers}.
 * <p>
 * The auto-configuration here is responsible for both imperative and reactive health checks. The reactive health check
 * has precedence over the imperative one.
 *
 * @author Michael J. Simons
 * @soundtrack Iron Maiden - Somewhere In Time
 */
@Configuration
@ConditionalOnClass({ Driver.class, Health.class })
@ConditionalOnEnabledHealthIndicator("neo4j")
@AutoConfigureBefore(HealthIndicatorAutoConfiguration.class)
@AutoConfigureAfter({ Neo4jDriverAutoConfiguration.class })
@ConditionalOnBean({ Driver.class })
public class Neo4jHealthIndicatorAutoConfiguration {

	@Configuration
	@ConditionalOnClass(HealthIndicator.class)
	static class Neo4jHealthIndicatorConfiguration
		extends CompositeHealthIndicatorConfiguration<Neo4jHealthIndicator, Driver> {

		@Bean
		// If Neo4jReactiveHealthIndicatorConfiguration kicked in, don't add the imperative version as well
		@ConditionalOnMissingBean(name = "neo4jHealthIndicator")
		public HealthIndicator neo4jHealthIndicator(Map<String, Driver> drivers) {
			return createHealthIndicator(drivers);
		}
	}

	@Configuration
	@ConditionalOnClass({ ReactiveHealthIndicator.class, Flux.class })
	static class Neo4jReactiveHealthIndicatorConfiguration
		extends CompositeReactiveHealthIndicatorConfiguration<Neo4jReactiveHealthIndicator, Driver> {

		@Bean
		public ReactiveHealthIndicator neo4jHealthIndicator(Map<String, Driver> drivers) {
			return createHealthIndicator(drivers);
		}
	}
}
