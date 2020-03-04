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
package org.neo4j.driver.springframework.boot.actuate;

import org.neo4j.driver.summary.ResultSummary;

/**
 * A holder for a {@link ResultSummary result summary} and database edition.
 *
 * @author Michael J. Simons
 * @soundtrack Black Sabbath - The End
 * @since 4.0.0.2
 */
final class ResultSummaryWithEdition {

	final ResultSummary resultSummary;

	final String edition;

	ResultSummaryWithEdition(ResultSummary resultSummary, String edition) {
		this.resultSummary = resultSummary;
		this.edition = edition;
	}


}
