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
package org.neo4j.doc.driver.springframework.boot.simple;

// tag::simple-example[]

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DisplayMoviesComponent implements CommandLineRunner {

	private final static Logger LOGGER = LoggerFactory.getLogger(DisplayMoviesComponent.class);

	private final Driver driver;

	public DisplayMoviesComponent(Driver driver) {
		this.driver = driver;
	}

	@Override
	public void run(String... args) {

		LOGGER.info("All the movies:");
		try (Session session = driver.session()) {
			session.run("MATCH (m:Movie) RETURN m ORDER BY m.name ASC").stream()
				.map(r -> r.get("m").asNode())
				.map(n -> n.get("title").asString())
				.forEach(LOGGER::info);
		}
	}
}
// end::simple-example[]
