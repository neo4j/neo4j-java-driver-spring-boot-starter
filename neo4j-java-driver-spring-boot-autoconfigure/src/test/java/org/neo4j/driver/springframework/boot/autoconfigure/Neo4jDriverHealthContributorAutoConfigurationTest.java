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
import static org.mockito.Mockito.*;
import static org.neo4j.driver.springframework.boot.test.Neo4jDriverMocks.*;

import reactor.core.publisher.Flux;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.driver.springframework.boot.actuate.Neo4jHealthIndicator;
import org.neo4j.driver.springframework.boot.actuate.Neo4jReactiveHealthIndicator;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.boot.actuate.autoconfigure.health.HealthContributorAutoConfiguration;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.PingHealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Michael J. Simons
 */
class Neo4jDriverHealthContributorAutoConfigurationTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(
			AutoConfigurations.of(
				HealthContributorAutoConfiguration.class,
				Neo4jDriverHealthContributorAutoConfiguration.class,
				org.springframework.boot.actuate.autoconfigure.neo4j.Neo4jHealthContributorAutoConfiguration.class
			));

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
					.hasSingleBean(PingHealthIndicator.class)
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
					Health health = context.getBean("neo4jHealthIndicator", HealthIndicator.class).health();
					assertThat(health.getDetails()).containsOnly(entry("test", true));
				});
		}
	}

	@Nested
	class Matches {

		@Test
		void ogmHealthIndicatorShouldHavePrecedence() {
			contextRunner
				.withUserConfiguration(WithDriver.class, WithSessionFactory.class)
				.withClassLoader(new FilteredClassLoader(Flux.class))
				.run(ctx -> {
					assertThat(ctx)
						.doesNotHaveBean(Neo4jHealthIndicator.class)
						.hasSingleBean(org.springframework.boot.actuate.neo4j.Neo4jHealthIndicator.class);
				});
		}

		@Test
		void shouldCreateHealthIndicator() {
			contextRunner
				.withUserConfiguration(WithDriver.class)
				.withClassLoader(new FilteredClassLoader(Flux.class))
				.run(ctx -> assertThat(ctx)
					.hasSingleBean(Neo4jHealthIndicator.class)
					.doesNotHaveBean(Neo4jReactiveHealthIndicator.class)
					.doesNotHaveBean(org.springframework.boot.actuate.neo4j.Neo4jHealthIndicator.class)
				);
		}
	}

	@Test
	void reactiveHealthCheckShouldHavePrecedence() {
		contextRunner
			.withUserConfiguration(WithDriver.class)
			.withClassLoader(new FilteredClassLoader(SessionFactory.class))
			.run(ctx -> {
					assertThat(ctx)
						.doesNotHaveBean(Neo4jHealthIndicator.class)
						.hasBean("neo4jHealthContributor");

					assertThat(ctx).getBean("neo4jHealthContributor")
						.isInstanceOf(CompositeReactiveHealthContributor.class)
						.satisfies(o -> {
							CompositeReactiveHealthContributor chc = (CompositeReactiveHealthContributor) o;
							assertThat(chc.getContributor("driver")).isInstanceOf(Neo4jReactiveHealthIndicator.class);
						});
				}

			);
	}

	@Configuration(proxyBeanMethods = false)
	static class WithDriver {

		@Bean
		Driver driver() {
			return mockDriverWithoutMetrics();
		}
	}

	@Configuration(proxyBeanMethods = false)
	static class WithSessionFactory {

		@Bean
		SessionFactory sessionFactory() {
			return mock(SessionFactory.class);
		}
	}

	@Configuration(proxyBeanMethods = false)
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
