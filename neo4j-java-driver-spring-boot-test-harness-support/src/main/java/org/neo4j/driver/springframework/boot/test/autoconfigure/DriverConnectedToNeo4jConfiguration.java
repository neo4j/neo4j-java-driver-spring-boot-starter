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

import static org.neo4j.driver.springframework.boot.test.autoconfigure.DriverConnectedToNeo4jConfiguration.*;
import static org.neo4j.driver.springframework.boot.test.autoconfigure.Neo4jTestHarnessAutoConfiguration.*;

import java.lang.reflect.Method;
import java.net.URI;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.springframework.boot.autoconfigure.Neo4jDriverProperties;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Finds a bean of type {@code org.neo4j.harness.Neo4j} and connects the driver accordingly.
 *
 * @author Michael J. Simons
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = RESOLVABLE_BEAN_TYPE)
@ConditionalOnBean(type = RESOLVABLE_BEAN_TYPE)
class DriverConnectedToNeo4jConfiguration {

	static final String RESOLVABLE_BEAN_TYPE = "org.neo4j.harness.Neo4j";

	@Bean
	Driver neo4jDriver(final Neo4jDriverProperties driverProperties, final ListableBeanFactory beanFactory) {

		LOG.debug("Using Neo4j test harness 4.0");

		Class<?> embeddedServerClass = ClassUtils
			.resolveClassName(RESOLVABLE_BEAN_TYPE, ClassUtils.getDefaultClassLoader());
		Object embeddedServerInstance = beanFactory.getBean(embeddedServerClass);
		Method boltURI = ReflectionUtils.findMethod(embeddedServerClass, "boltURI");

		return GraphDatabase.driver(
			(URI) ReflectionUtils.invokeMethod(boltURI, embeddedServerInstance),
			AuthTokens.none(),
			driverProperties.asDriverConfig());
	}
}
