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
package org.neo4j.doc.driver.springframework.boot.embedded;

// tag::EmbeddedConfig[]

import static java.lang.System.Logger.Level.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.System.Logger;
import java.net.URI;
import java.nio.file.Files;

import org.neo4j.configuration.connectors.BoltConnector;
import org.neo4j.configuration.helpers.SocketAddress;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.driver.springframework.boot.autoconfigure.Neo4jDriverProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;

// end::EmbeddedConfig[]

/**
 * This is one way to create an embedded Neo4j instance inside a Spring Boot application. You can either use the
 * Bolt connector and <a href="https://github.com/neo4j/neo4j-java-driver-spring-boot-starter">neo4j-java-driver-spring-boot-starter</a>
 * to connect to this instance of use the Graph API as you wish.
 * <p>
 * This config is written in such a way with the condition, that the driver will always point to this instance.
 *
 * @author Michael J. Simons
 */
// tag::EmbeddedConfig[]
@Configuration(proxyBeanMethods = false) // <.>
@EnableConfigurationProperties(DatabaseManagementServiceProperties.class)  // <.>
public class Neo4jConfig {

	// end::EmbeddedConfig[]
	// The System logger is available since Java 9. It delegates to a concrete
	// underlying logging framework. If you're still on JDK 8, feel free to replace
	// it with another facade like Slf4j or implementation like java.util.logging Log4j2.
	// The driver will log through Springs abstraction anyway.
	// tag::EmbeddedConfig[]
	private static final Logger LOGGER = System.getLogger(Neo4jConfig.class.getName());

	// end::EmbeddedConfig[]
	/**
	 * This creates a bean of type {@link DatabaseManagementService}.
	 * Its {@link DatabaseManagementService#shutdown() shutdown-method}
	 * will be automatically called by the Spring container.
	 * <p>
	 * We pass in the {@link Neo4jDriverProperties driver properties} as well as
	 * the {@link DatabaseManagementServiceProperties database management service properties}
	 *
	 * @param driverProperties
	 * @param serviceProperties
	 * @return A database management service.
	 */
	// tag::EmbeddedConfig[]
	@Bean // <.>
	@Conditional(OnDriverCouldConnectToEmbedded.class) // <.>
	DatabaseManagementService databaseManagementService(
		Neo4jDriverProperties driverProperties,
		DatabaseManagementServiceProperties serviceProperties
	) {
		// <.>
		var homeDirectory = serviceProperties.getHomeDirectory().orElseGet(() -> {
			try {
				return Files.createTempDirectory("neo4j").toFile();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
		LOGGER.log(
			INFO,
			"Configuring {0} as home directory for the embedded instance.",
			homeDirectory
		);

		var driverPort = driverProperties.getUri().getPort();
		var address = new SocketAddress("localhost", driverPort); // <.>
		return new DatabaseManagementServiceBuilder(homeDirectory)
			.setConfig(BoltConnector.enabled, true)
			.setConfig(BoltConnector.listen_address, address)
			.build();
	}

	static class OnDriverCouldConnectToEmbedded implements Condition { // <.>

		@Override
		public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {

			// @formatter:off
			boolean outcome = false;
			URI configuredBoltUri = context.getEnvironment().getProperty("org.neo4j.driver.uri", URI.class);
			if (configuredBoltUri == null) {
				LOGGER.log(WARNING, "Driver is not configured, skipping embedded instance as well.");
			} else if (!"bolt".equalsIgnoreCase(configuredBoltUri.getScheme())) {
				LOGGER.log(WARNING, "Driver configured to use `{0}`, but embedded only supports bolt. Not configuring embedded.",  configuredBoltUri);
			} else if (!"localhost".equalsIgnoreCase(configuredBoltUri.getHost())) {
				LOGGER.log(WARNING, "Driver configured to connect to `{0}`, not a local instance. Not configuring embedded.", configuredBoltUri.getHost());
			} else {
				outcome = true;
			}
			// @formatter:on

			return outcome;
		}
	}
}
