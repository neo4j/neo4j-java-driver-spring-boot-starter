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
package org.neo4j.driver.springframework.boot.test.autoconfigure;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.springframework.boot.autoconfigure.Neo4jDriverAutoConfiguration;
import org.neo4j.driver.springframework.boot.autoconfigure.Neo4jDriverProperties;
import org.neo4j.harness.Neo4j;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Automatic configuration that provides a driver that is connected to an existing instance of {@link Neo4j Neo4j test harness}
 * when there's no driver configured elsewhere.
 *
 * @author Michael J. Simons
 * @soundtrack NMZS - Egotrip
 * @since 4.0.0.1
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(Neo4jDriverAutoConfiguration.class)
@ConditionalOnClass(Neo4j.class)
@ConditionalOnBean(Neo4j.class)
@ConditionalOnMissingBean(Driver.class)
@EnableConfigurationProperties(Neo4jDriverProperties.class)
public class Neo4jTestHarnessAutoConfiguration {

	@Bean

	Driver neo4jDriver(final Neo4jDriverProperties driverProperties, final Neo4j neo4j) {
		return GraphDatabase.driver(neo4j.boltURI(), AuthTokens.none(), driverProperties.asDriverConfig());
	}
}
