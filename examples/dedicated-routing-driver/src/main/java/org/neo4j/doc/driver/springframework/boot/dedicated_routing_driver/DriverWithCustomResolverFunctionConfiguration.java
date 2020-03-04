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
package org.neo4j.doc.driver.springframework.boot.dedicated_routing_driver;

import java.util.Arrays;
import java.util.HashSet;

import org.neo4j.driver.AuthToken;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.net.ServerAddress;
import org.neo4j.driver.springframework.boot.autoconfigure.Neo4jDriverProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * This is a setup for the solution proposeed in the official driver docs:
 * <a href="https://neo4j.com/docs/driver-manual/current/client-applications/#driver-resolver-function">2.2.1.3. Resolver function</a>
 */
// This is just here to prevent both this configuration and RoutingDriverConfiguration to run in parallel.
@Profile("custom-resolver-function")

@Configuration
public class DriverWithCustomResolverFunctionConfiguration {

	@Bean
	public Driver neo4jDriver(Neo4jDriverProperties neo4jDriverProperties) {

		Config config = Config.builder()
			.withResolver(address -> {
				if ("datacenter1".equals(address.host())) {
					return new HashSet<>(
						Arrays.asList(ServerAddress.of("dc1-core1", 7687), ServerAddress.of("dc1-core2", 7687)));
				} else {
					return new HashSet<>(Arrays.asList(ServerAddress.of("other-core", 7687)));
				}
			}).build();
		AuthToken authToken = neo4jDriverProperties.getAuthToken();

		return GraphDatabase.driver(neo4jDriverProperties.getUri(), authToken, config);
	}
}
