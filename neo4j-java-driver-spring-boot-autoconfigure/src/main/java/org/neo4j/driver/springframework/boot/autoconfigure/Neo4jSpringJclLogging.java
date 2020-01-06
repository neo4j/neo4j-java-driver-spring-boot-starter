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
package org.neo4j.driver.springframework.boot.autoconfigure;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.neo4j.driver.Logger;
import org.neo4j.driver.Logging;

/**
 * Shim to use Spring JCL implementation, delegating all the hard work of deciding the underlying system to Spring and Spring Boot.
 *
 * @author Michael J. Simons
 */
public final class Neo4jSpringJclLogging implements Logging {

	/**
	 * This prefix gets added to the log names the driver requests to add some namespace around it in a bigger application scenario.
	 */
	private static final String AUTOMATIC_PREFIX = "org.neo4j.driver.";

	@Override
	public Logger getLog(String name) {

		String requestedLog = name;
		if (!requestedLog.startsWith(AUTOMATIC_PREFIX)) {
			requestedLog = AUTOMATIC_PREFIX + name;
		}
		Log springJclLog = LogFactory.getLog(requestedLog);
		return new SpringJclLogger(springJclLog);
	}

	static final class SpringJclLogger implements Logger {

		private final Log delegate;

		SpringJclLogger(Log delegate) {
			this.delegate = delegate;
		}

		@Override
		public void error(String message, Throwable cause) {
			this.delegate.error(message, cause);
		}

		@Override
		public void info(String format, Object... params) {
			this.delegate.info(String.format(format, params));
		}

		@Override
		public void warn(String format, Object... params) {
			this.delegate.warn(String.format(format, params));
		}

		@Override
		public void warn(String message, Throwable cause) {
			this.delegate.warn(message, cause);
		}

		@Override
		public void debug(String format, Object... params) {
			if (isDebugEnabled()) {
				this.delegate.debug(String.format(format, params));
			}
		}

		@Override
		public void trace(String format, Object... params) {
			if (isTraceEnabled()) {
				this.delegate.trace(String.format(format, params));
			}
		}

		@Override
		public boolean isTraceEnabled() {
			return this.delegate.isTraceEnabled();
		}

		@Override
		public boolean isDebugEnabled() {
			return this.delegate.isDebugEnabled();
		}
	}
}

