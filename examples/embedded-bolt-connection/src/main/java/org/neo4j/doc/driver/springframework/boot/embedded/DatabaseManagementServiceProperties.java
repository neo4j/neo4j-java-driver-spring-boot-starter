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

// tag::EmbeddedConfig-Properties[]
import java.io.File;
import java.util.Optional;

import org.springframework.boot.context.properties.ConfigurationProperties;

// end::EmbeddedConfig-Properties[]
/**
 * This is a Spring properties component. Every property in an {@code application.properties} or from the environment
 * prefixed with {@literal org.neo4j.database-management-service} will be mapped into this class.
 * {@literal org.neo4j.database-management-service} is the canonical format, but {@literal org.neo4j.databaseManagementService}
 * will work as well.
 * <p>
 * This would be the place to add additional properties you might want to configure with your embedded instance.
 */
// tag::EmbeddedConfig-Properties[]
@ConfigurationProperties(prefix = "org.neo4j.database-management-service") // <.>
public class DatabaseManagementServiceProperties {

	/**
	 * The default home directory. If not configured, a random, temporary directory is used.
	 * If you want to keep your data around, please set it to a writeable path in your configuration.
	 */
	private Optional<File> homeDirectory = Optional.empty(); // <.>

	public Optional<File> getHomeDirectory() {
		return homeDirectory;
	}

	public void setHomeDirectory(Optional<File> homeDirectory) {
		this.homeDirectory = homeDirectory;
	}
}
