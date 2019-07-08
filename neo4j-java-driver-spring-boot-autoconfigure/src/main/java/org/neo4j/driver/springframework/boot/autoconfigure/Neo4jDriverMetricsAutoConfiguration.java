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

import io.micrometer.core.instrument.MeterRegistry;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.neo4j.driver.Driver;
import org.neo4j.driver.springframework.boot.actuate.Neo4jDriverMetrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for metrics on all available
 * {@link org.neo4j.driver.Driver drivers}.
 * <p>
 * The reason we are doing this dance with the manual binding is the fact that this autonconfiguration
 * should work with more than one instance of the driver. If a user has multiple instances configured,
 * than each instance should be bound via the binder to registry. Without that requirement, we could
 * just add a {@link Bean @Bean} of type {@link Neo4jDriverMetrics} to the context and be done.
 *
 * @author Michael J. Simons
 */
@Configuration
@AutoConfigureAfter({ MetricsAutoConfiguration.class, Neo4jDriverAutoConfiguration.class,
	SimpleMetricsExportAutoConfiguration.class })
@ConditionalOnClass({ Driver.class, MeterRegistry.class })
@ConditionalOnBean({ Driver.class, MeterRegistry.class })
public class Neo4jDriverMetricsAutoConfiguration {

	private static final Log logger = LogFactory.getLog(Neo4jDriverMetricsAutoConfiguration.class);

	@Autowired
	public void bindDataSourcesToRegistry(Map<String, Driver> drivers, MeterRegistry registry) {

		drivers.forEach((name, driver) -> {
			if (!Neo4jDriverMetrics.metricsAreEnabled(driver)) {
				return;
			}
			driver
				.verifyConnectivityAsync()
				.thenRunAsync(() -> new Neo4jDriverMetrics(name, driver, Collections.emptyList()).bindTo(registry))
				.exceptionally(e -> {
					logger.warn("Could not verify connection for " + driver + " and thus not bind to metrics.", e);
					return null;
				});
		});
	}
}
