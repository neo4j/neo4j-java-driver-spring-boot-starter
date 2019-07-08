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
package org.neo4j.doc.driver.springframework.boot.web;

// tag::reactive-web-example[]

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.neo4j.driver.Driver;
import org.neo4j.driver.reactive.RxSession;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DisplayMoviesController {

	private final Driver driver;

	public DisplayMoviesController(Driver driver) {
		this.driver = driver;
	}

	@GetMapping(path = "/movies", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<String> getMovieTitles() {

		return Flux.usingWhen(
			Mono.fromSupplier(() -> driver.rxSession()),
			s -> Flux.from(s.run("MATCH (m:Movie) RETURN m ORDER BY m.name ASC").records()),
			RxSession::close
		).map(r -> r.get("m").asNode().get("title").asString());
	}
}
// end::reactive-web-example[]
