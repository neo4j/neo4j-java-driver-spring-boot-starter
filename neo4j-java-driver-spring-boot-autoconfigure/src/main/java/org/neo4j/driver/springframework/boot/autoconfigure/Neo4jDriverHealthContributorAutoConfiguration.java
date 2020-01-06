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
package org.neo4j.driver.springframework.boot.autoconfigure;

import reactor.core.publisher.Flux;

import java.util.Map;

import org.neo4j.driver.Driver;
import org.neo4j.driver.springframework.boot.actuate.Neo4jHealthIndicator;
import org.neo4j.driver.springframework.boot.actuate.Neo4jReactiveHealthIndicator;
import org.springframework.boot.actuate.autoconfigure.health.CompositeHealthContributorConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.CompositeReactiveHealthContributorConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.autoconfigure.health.HealthContributorAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.neo4j.Neo4jHealthContributorAutoConfiguration;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

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
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ Driver.class, Health.class })
@ConditionalOnEnabledHealthIndicator("neo4j")
@AutoConfigureBefore(HealthContributorAutoConfiguration.class)
@AutoConfigureAfter({ Neo4jDriverAutoConfiguration.class, Neo4jDataAutoConfiguration.class,
	Neo4jHealthContributorAutoConfiguration.class })
@ConditionalOnBean({ Driver.class })
public class Neo4jDriverHealthContributorAutoConfiguration {

	@Configuration(proxyBeanMethods = false)
	@Order(-20)
	static class Neo4jHealthIndicatorConfiguration
		extends CompositeHealthContributorConfiguration<Neo4jHealthIndicator, Driver> {

		@Bean
		// If Neo4jReactiveHealthIndicatorConfiguration kicked in, don't add the imperative version as well
		@ConditionalOnMissingBean(name = "neo4jHealthContributor")
		public HealthContributor neo4jHealthContributor(Map<String, Driver> drivers) {
			return createContributor(drivers);
		}
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass({ Flux.class })
	@Order(-30)
	static class Neo4jReactiveHealthIndicatorConfiguration
		extends CompositeReactiveHealthContributorConfiguration<Neo4jReactiveHealthIndicator, Driver> {

		@Bean
		@ConditionalOnMissingBean(name = "neo4jHealthContributor")
		public ReactiveHealthContributor neo4jHealthContributor(Map<String, Driver> drivers) {
			return createComposite(drivers);
		}
	}
}
