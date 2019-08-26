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

import java.util.Map;

import org.neo4j.driver.springframework.boot.actuate.Neo4jHealthIndicator;
import org.neo4j.driver.v1.Driver;
import org.springframework.boot.actuate.autoconfigure.health.CompositeHealthIndicatorConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.autoconfigure.health.HealthIndicatorAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.neo4j.Neo4jHealthIndicatorAutoConfiguration;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
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
 * {@link org.neo4j.driver.v1.Driver drivers}.
 *
 * @author Michael J. Simons
 * @soundtrack Iron Maiden - Somewhere In Time
 */
@Configuration
@ConditionalOnClass({ Health.class, Driver.class })
@ConditionalOnEnabledHealthIndicator("neo4j")
@ConditionalOnBean(Driver.class)
@AutoConfigureBefore({ HealthIndicatorAutoConfiguration.class })
@AutoConfigureAfter({ Neo4jDriverAutoConfiguration.class, Neo4jDataAutoConfiguration.class,
	Neo4jHealthIndicatorAutoConfiguration.class })
@ConditionalOnMissingBean(name = "neo4jHealthIndicator")
public class Neo4jDriverHealthIndicatorAutoConfiguration {

	@Order(-20)
	static class Neo4jNeo4jHealthIndicatorConfiguration
		extends CompositeHealthIndicatorConfiguration<Neo4jHealthIndicator, Driver> {

		@Bean
		HealthIndicator neo4jHealthIndicator(Map<String, Driver> drivers) {
			return createHealthIndicator(drivers);
		}

	}
}
