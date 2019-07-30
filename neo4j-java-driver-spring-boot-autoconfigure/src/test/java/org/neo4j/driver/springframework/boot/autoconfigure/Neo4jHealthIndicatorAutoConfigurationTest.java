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

import static org.assertj.core.api.Assertions.*;
import static org.neo4j.driver.springframework.boot.test.Neo4jDriverMocks.*;

import reactor.core.publisher.Flux;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.driver.springframework.boot.actuate.Neo4jHealthIndicator;
import org.neo4j.driver.springframework.boot.actuate.Neo4jReactiveHealthIndicator;
import org.springframework.boot.actuate.autoconfigure.health.HealthIndicatorAutoConfiguration;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.ApplicationHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Michael J. Simons
 */
class Neo4jHealthIndicatorAutoConfigurationTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(
			AutoConfigurations.of(HealthIndicatorAutoConfiguration.class, Neo4jHealthIndicatorAutoConfiguration.class));

	@Nested
	class NoMatches {
		@Test
		void shouldRespectManagementDecisions() {
			contextRunner
				.withUserConfiguration(WithDriver.class)
				.withPropertyValues("management.health.neo4j.enabled=false")
				.run(ctx -> assertThat(ctx)
					.doesNotHaveBean(Neo4jHealthIndicator.class)
					.doesNotHaveBean(Neo4jReactiveHealthIndicator.class)
					.hasSingleBean(ApplicationHealthIndicator.class)
				);
		}

		@Test
		void shouldRequireHealthClass() {
			contextRunner
				.withUserConfiguration(WithDriver.class)
				.withClassLoader(new FilteredClassLoader(Health.class))
				.run(ctx -> assertThat(ctx)
					.doesNotHaveBean(Neo4jHealthIndicator.class)
					.doesNotHaveBean(Neo4jReactiveHealthIndicator.class)
				);
		}

		@Test
		void shouldRequireDriverClass() {
			contextRunner
				.withUserConfiguration(WithDriver.class)
				.withClassLoader(new FilteredClassLoader(Driver.class))
				.run(ctx -> assertThat(ctx)
					.doesNotHaveBean(Neo4jHealthIndicator.class)
					.doesNotHaveBean(Neo4jReactiveHealthIndicator.class)
				);
		}

		@Test
		void shouldRequireDriverBean() {
			contextRunner
				.run(ctx -> assertThat(ctx)
					.doesNotHaveBean(Neo4jHealthIndicator.class)
					.doesNotHaveBean(Neo4jReactiveHealthIndicator.class)
				);
		}

		@Test
		void shouldRequireHealthIndicatorClasses() {
			contextRunner
				.withUserConfiguration(WithDriver.class)
				.withClassLoader(
					new FilteredClassLoader(Health.class, Flux.class))
				.run(ctx -> assertThat(ctx)
					.doesNotHaveBean(Neo4jHealthIndicator.class)
					.doesNotHaveBean(Neo4jReactiveHealthIndicator.class)
				);

			contextRunner
				.withUserConfiguration(WithDriver.class)
				.withClassLoader(new FilteredClassLoader(Flux.class))
				.run(ctx -> assertThat(ctx)
					.hasSingleBean(Neo4jHealthIndicator.class)
					.doesNotHaveBean(Neo4jReactiveHealthIndicator.class)
				);
		}

		@Test
		void defaultIndicatorCanBeReplaced() {
			contextRunner
				.withUserConfiguration(WithDriver.class, WithCustomIndicator.class)
				.run((context) -> {
					assertThat(context).hasBean("neo4jHealthIndicator");
					assertThat(context).doesNotHaveBean(ApplicationHealthIndicator.class);
					Health health = context.getBean("neo4jHealthIndicator", HealthIndicator.class).health();
					assertThat(health.getDetails()).containsOnly(entry("test", true));
				});
		}
	}

	@Test
	void reactiveHealthCheckShouldHavePrecedence() {
		contextRunner
			.withUserConfiguration(WithDriver.class)
			.run(ctx -> assertThat(ctx)
				.doesNotHaveBean(Neo4jHealthIndicator.class)
				.hasSingleBean(Neo4jReactiveHealthIndicator.class)
			);
	}

	@Configuration
	static class WithDriver {

		@Bean
		Driver driver() {
			return mockDriverWithoutMetrics();
		}
	}

	@Configuration
	static class WithCustomIndicator {

		@Bean
		HealthIndicator neo4jHealthIndicator() {
			return new AbstractHealthIndicator() {

				protected void doHealthCheck(Health.Builder builder) throws Exception {
					builder.up().withDetail("test", true);
				}
			};
		}

	}
}
